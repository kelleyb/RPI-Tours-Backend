package controllers

import javax.inject._
import play.api._
import play.api.libs.json._  
import play.api.mvc._
import play.api.mvc.Results._


import models._
import dal._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.forkjoin._
import scala.language.postfixOps

import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's API.
 */
@Singleton
class ApiController @Inject()(
  categories: CategoryRepository,
  landmarks: LandmarkRepository,
  photos: PhotoRepository,
  tours: TourRepository,
  waypoints: WaypointRepository,
  landmarkPhotos: LandmarkPhotoRepository,
  tourCategories: TourCategoryRepository,
  tourLandmarks: TourLandmarkRepository) extends Controller {


  /**
   * Given a landmark ID, get the JSON for the photos for that given landmark.
   */
  def findPhotosByLandmarkId(landmarkId: Long): JsValue = {
    Json.toJson(Await.result({
      landmarkPhotos.findByLandmarkId(landmarkId).map { photoIds =>
        photoIds.map { photoId =>
          Await.result(photos.findById(photoId), 1 second)
        }
      }
    }, 1 second))
  }

  /**
   * Given a tour ID, find all the landmarks that are a part of that tour.
   * Unfortunately, we need to go through a secondary table for this. Not too
   * bad, though.
   */
  def findLandmarksByTourId(tourId: Long): JsValue = {
    Json.toJson(Await.result({
      tourLandmarks.findByTourId(tourId).map { landmarkIds =>
        landmarkIds.map { landmarkId =>
          Await.result(landmarks.findById(landmarkId).map { landmark =>
            Json.toJson(landmark).as[JsObject] + 
              ("photos" -> findPhotosByLandmarkId(landmarkId))
          }, 1 second)
        }
      }
    }, 1 second))
  }

  /**
   * Find the tour by its ID and return it along with landmark/waypoint data.
   */
  def findTourById(tourId: Long): JsValue = {
    Await.result(tours.findById(tourId).map { tour =>
      Json.toJson(tour).as[JsObject] + 
        // Now that we have the actual tour objects, we can add waypoint and
        // landmark data. Waypoints must be in a specific order, landmarks
        // don't matter quite so much.
        ("waypoints" -> Json.toJson {
          Await.result(waypoints.findByTourId(tour.id), 1 second)
        }) +
        ("landmarks" -> Json.toJson(findLandmarksByTourId(tour.id)))
    }, 1 second)
  }

  /**
   * Given a category ID, find how many tours that category has.
   */
  def findNumToursForCategory(categoryId: Long): JsValue = {
    Json.toJson(Await.result({
      tourCategories.findByCategoryId(categoryId).map(_.length)
    }, 1 second))
  }


  /**
   * GET /api/v1/tours
   * GET all the tours in the database.
   */
  def getTours = Action.async { implicit request =>
    tours.list.map {
      case allTours: Seq[Tour] => Ok(Json.toJson {
        allTours.map(t => findTourById(t.id))
      })
    }.recover { case t => 
      InternalServerError("An error occurred: " + t.getMessage)
    }
  }

  /**
   * GET /api/v1/tours/:id
   * Get the tour with given ID (includes landmark, photo, and waypoint info).
   */
  def getTour(id: Long) = Action.async { implicit request =>
    tours.findById(id).map(t => Ok(findTourById(t.id)))
  }

  /**
   * GET /api/v1/categories/:id
   * Get the category with given ID
   */
  def getCategory(id: Long) = Action.async { implicit request =>
    categories.findById(id).map { category =>
      Ok(Json.toJson(category).as[JsObject] +
        ("availableTours" -> findNumToursForCategory(category.id)))
    }
  }

  /**
   * GET /api/v1/categories
   * Get all the categories in the database, as well as the number of tours
   * available within that category.
   */
  def getCategories = Action.async { implicit request =>
    categories.list.map {
      case allCategories: Seq[Category] => Ok(Json.toJson {
        allCategories.map { category =>
          Json.toJson(category).as[JsObject] + 
            ("availableTours" ->  findNumToursForCategory(category.id))
        }
      })
    }.recover { case t =>
      InternalServerError("An error occurred: " + t.getMessage)
    }
  }

  /**
   * GET /api/v1/categories/:id/tours
   * Get all the tours which belong to the given category.
   */
  def getToursForCategory(id: Long) = Action.async { implicit request =>
    tourCategories.findByCategoryId(id).map { tourIds =>
      Ok(Json.toJson(
        tourIds.map { tourId =>
          Await.result(tours.findById(tourId), 1 second)
        }))
    }
  }
}
