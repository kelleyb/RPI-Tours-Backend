package controllers

import javax.inject._
import play.api._
import play.api.libs.json._  
import play.api.mvc._
import play.api.mvc.Results._


import models._
import dal._

import scala.concurrent.{ ExecutionContext, Future}
import scala.concurrent.forkjoin._
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
  tourLandmarks: TourLandmarkRepository,
  tourWaypoints: TourWaypointRepository) extends Controller {

  def getTours = Action.async { implicit request =>
    tours.list.map(allTours => Ok(Json.toJson(allTours)))
      .recover { case t => 
        InternalServerError("An error occured: " + t.getMessage)
      }
  }
}
