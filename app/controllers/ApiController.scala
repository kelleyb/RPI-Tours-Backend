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
 * application's home page.
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
   * GET all the tours in the database.
   */
  def getTours = Action.async { implicit request =>
    tours.list.map(allTours => Ok(Json.toJson(allTours)))
      .recover { case t => 
        InternalServerError("An error occured: " + t.getMessage)
      }
  }

  /**
   * GET all the categories in the database, as well as the number of tours
   * available within that category.
   */
  def getCategories = Action.async { implicit request => 
    categories.list.map { allCategories =>
      Ok(Json.toJson(
        allCategories.map { cat =>
          Json.toJson(cat).as[JsObject] + ("availableTours" -> Json.toJson(
            // We need to get the number of tours for the category, so
            // get all the tourCategories with the current categoryId
            Await.result(
              tourCategories.findByCategoryId(cat.id).map(_.length),
              1 second)
          )) // Added the number of available tours to the JSON
        }
      )) // OK
    }.recover { case t =>
      // ERROR
      InternalServerError("An error occured: " + t.getMessage)
    }
  }
}
