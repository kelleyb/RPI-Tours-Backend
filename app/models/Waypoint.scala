package models

import play.api.libs.json._

case class Waypoint(
  id: Long, 
  lat: Double, 
  long: Double, 
  tourId: Long, 
  ordering: Int)

/**
 * Companion object for Waypoint class. 
 */
object Waypoint {
  implicit val waypointFormat = Json.format[Waypoint]
}
