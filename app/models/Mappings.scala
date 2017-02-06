package models

import play.api.libs.json._

/*
 * Table mappings for relationships (many to many, etc)
 */

case class TourCategory(tourId: Long, categoryId: Long)
case class TourWaypoint(tourId: Long, waypointId: Long)
case class TourLandmark(tourId: Long, landmarkId: Long, ordering: Int)
case class LandmarkPhoto(landmarkId: Long, photoId: Long)


object TourCategory {
  implicit val tcFormat = Json.format[TourCategory]
}

object TourWaypoint {
  implicit val twFormat = Json.format[TourWaypoint]
}

object TourLandmark {
  implicit val tlFormat = Json.format[TourLandmark]
}

object LandmarkPhoto {
  implicit val lpFormat = Json.format[LandmarkPhoto]
}
