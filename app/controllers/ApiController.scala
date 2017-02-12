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

  def successCode(): JsObject = JsObject(Seq(
        ("status") -> JsString("success") 
      )
    )
  def errMsg(msg: String): JsObject = JsObject(
          Seq(
            ("status" -> JsString("failure")),
            ("error" -> JsString(msg))
          )
        )

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
    Await.result(tours.findById(tourId).map {
      case Some(tour) => Json.toJson(tour).as[JsObject] +
        // Now that we have the actual tour objects, we can add waypoint and
        // landmark data. Waypoints must be in a specific order, landmarks
        // don't matter quite so much.
        ("waypoints" -> Json.toJson {
          Await.result(waypoints.findByTourId(tour.id), 1 second)
        }) +
        ("landmarks" -> Json.toJson(findLandmarksByTourId(tour.id)))
        
      case None => errMsg(s"could not find tour with ID ${tourId}")
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
      case allTours: Seq[Tour] => Ok(Json.obj(
        ("content" -> allTours.map(t => findTourById(t.id)))
      ) ++ successCode)
    }.recover { case t => 
      InternalServerError("An error occurred: " + t.getMessage)
    }
  }

  /**
   * GET /api/v1/tours/:id
   * Get the tour with given ID (includes landmark, photo, and waypoint info).
   */
  def getTour(id: Long) = Action.async { implicit request =>
    tours.findById(id)
      .map {
        case Some(tour) => Ok(
          Json.obj("content" -> findTourById(tour.id)) ++
          successCode
        )
        case None => NotFound(errMsg(s"Could not find tour with ID ${id}"))
      }
  }

  /**
   * GET /api/v1/categories/:id
   * Get the category with given ID
   */
  def getCategory(id: Long) = Action.async { implicit request =>
    categories.findById(id).map { 
      case Some(category) => Ok(Json.obj(
        "content" -> (Json.toJson(category).as[JsObject] +
          ("numAvailableTours" -> findNumToursForCategory(category.id)))) ++
        successCode)
      case None => NotFound(errMsg(s"Could not find category with ID ${id}"))
    }
  }

  /**
   * GET /api/v1/categories
   * Get all the categories in the database, as well as the number of tours
   * available within that category.
   */
  def getCategories = Action.async { implicit request =>
    categories.list.map {
      case allCategories: Seq[Category] => Ok(Json.obj( 
        ("content" -> allCategories.map { category =>
          Json.toJson(category).as[JsObject] + 
            ("numAvailableTours" ->  findNumToursForCategory(category.id))
        })
      ).as[JsObject] ++ successCode)
    }
  }

  /**
   * GET /api/v1/categories/:id/tours
   * Get all the tours which belong to the given category.
   */
  def getToursForCategory(id: Long) = Action.async { implicit request =>
    categories.findById(id).map {
      case Some(_) => Await.result(
        tourCategories.findByCategoryId(id).map { tourIds =>
          Ok(Json.obj(
            "content" -> tourIds.map { tourId =>
              findTourById(tourId)
            }).as[JsObject] ++ successCode)
        }, 1 second)
      case None => NotFound(errMsg(s"Could not find category with ID ${id}"))
    }
    
  }

  /**
   * GET /api/v1/tours/:id/last_updated
   * Get the last time the selected tour was updated
   */
  def getTimeTourLastUpdated(id: Long) = Action.async { implicit request =>
    tours.findById(id).map {
      case Some(tour) => Ok(Json.obj(
        ("content" -> JsObject(Seq("lastUpdated" -> JsString(tour.lastUpdated))))
      ) ++ successCode)
      case None => NotFound(errMsg(s"Could not find tour with ID ${id}"))
    }
  }

  /**
   * GET /api/v1/categories/:id/last_updated
   * Get the last time the selected category was updated
   */
  def getTimeCatLastUpdated(id: Long) = Action.async { implicit request =>
    categories.findById(id).map {
      case Some(cat) => Ok(Json.obj(
        ("content" -> JsObject(Seq("lastUpdated" -> JsString(cat.lastUpdated))))
      ) ++ successCode)
      case None => NotFound(errMsg(s"Could not find category with ID ${id}"))
    }
  }
}
