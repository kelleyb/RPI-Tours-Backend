package controllers

import javax.inject._
import play.api._
import play.api.libs.json._  
import play.api.mvc._
import play.api.mvc.Results._


import models._
import dal._

import scala.concurrent.{ExecutionContext, Future}
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
  def findPhotosByLandmarkId(landmarkId: Long): Future[JsValue] = {
    for {
      photoIds <- landmarkPhotos.findByLandmarkId(landmarkId)
      lp <- Future.sequence {
          photoIds.map { photoId =>
            photos.findById(photoId)
          }
        }
    } yield (Json.toJson(lp))
  }

  /**
   * Given a tour ID, find all the landmarks that are a part of that tour.
   * Unfortunately, we need to go through a secondary table for this. Not too
   * bad, though.
   */
  def findLandmarksByTourId(tourId: Long): Future[JsValue] = {
    for {
      landmarkIds <- tourLandmarks.findByTourId(tourId)
      ls <- Future.sequence {
          landmarkIds.map { landmarkId =>
            for {
              landmark <- landmarks.findById(landmarkId)
              photos <- findPhotosByLandmarkId(landmarkId)
            } yield {
              Json.toJson(landmark).as[JsObject] + 
                ("photos" -> Json.toJson(photos))
            }
          }
        }
    } yield (Json.toJson(ls))
  }

  /**
   * Find the tour by its ID and return it along with landmark/waypoint data.
   */
  def findTourById(tourId: Long): Future[JsValue] = {
    tours.findById(tourId).flatMap {
      case Some(tour) => 
        findLandmarksByTourId(tourId).flatMap { currLandmarks =>
          waypoints.findByTourId(tourId).map { wp =>
            Json.toJson(tour).as[JsObject] +
            // Now that we have the actual tour objects, we can add waypoint and
            // landmark data. Waypoints must be in a specific order, landmarks
            // don't matter quite so much.
            ("waypoints" -> Json.toJson(wp)) +
            ("landmarks" -> currLandmarks)
          }
        }
      case None => Future(errMsg(s"could not find tour with ID ${tourId}"))
    }
  }

  /**
   * Given a category ID, find how many tours that category has.
   */
  def findNumToursForCategory(categoryId: Long): Future[JsValue] = {
    tourCategories.findByCategoryId(categoryId).map(_.length).map { n =>
      Json.toJson(n)
    }
  }


  /**
   * GET /api/v1/tours
   * GET all the tours in the database.
   */
  def getTours = Action.async { implicit request =>
    tours.list.flatMap {
      case allTours: Seq[Tour] => 
        Future.sequence(allTours.map(t => findTourById(t.id))).map { tourJS =>
          Ok {
            Json.obj("content" -> tourJS) ++ successCode
          }
        }
    }
  }

  /**
   * GET /api/v1/tours/:id
   * Get the tour with given ID (includes landmark, photo, and waypoint info).
   */
  def getTour(id: Long) = Action.async { implicit request =>
    tours.findById(id)
      .flatMap {
        case Some(tour) => 
          findTourById(tour.id).map { t =>
            Ok {
              Json.obj("content" -> t) ++
              successCode
            }
          }
        case None => Future {
          NotFound(errMsg(s"Could not find tour with ID ${id}"))
        }
      }
  }

  /**
   * GET /api/v1/categories/:id
   * Get the category with given ID
   */
  def getCategory(id: Long) = Action.async { implicit request =>
    categories.findById(id).flatMap { 
      case Some(category) => 
        findNumToursForCategory(category.id).map { numTours =>
          Ok(Json.obj("content" -> (Json.toJson(category).as[JsObject] +
              ("numAvailableTours" -> numTours))) ++
            successCode)
        }
      
      case None => Future {
        NotFound(errMsg(s"Could not find category with ID ${id}"))
      }
    }
  }

  /**
   * GET /api/v1/categories
   * Get all the categories in the database, as well as the number of tours
   * available within that category.
   */
  def getCategories = Action.async { implicit request =>
    categories.list.flatMap {
      case allCategories: Seq[Category] => Future.sequence {
        allCategories.map { category =>
          findNumToursForCategory(category.id).map { numTours =>
            Json.toJson(category).as[JsObject] ++
            Json.obj("numAvailableTours" ->  numTours)
          }
        }
      }
    }.map { c =>
      Ok {
        Json.obj("content" -> Json.toJson(c)) ++ successCode
      }
    }
  }

  /**
   * GET /api/v1/categories/:id/tours
   * Get all the tours which belong to the given category.
   */
  def getToursForCategory(id: Long) = Action.async { implicit request =>
    categories.findById(id).flatMap {
      case Some(_) =>
        tourCategories.findByCategoryId(id).flatMap { tourIds =>
          Future.sequence {
            tourIds.map { tourId =>
              findTourById(tourId)
            }
          }.map { tcs =>
            Ok(Json.obj("content" -> tcs) ++ successCode)
          }
          
        }
      case None => Future {
        NotFound(errMsg(s"Could not find category with ID ${id}"))
      }
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
