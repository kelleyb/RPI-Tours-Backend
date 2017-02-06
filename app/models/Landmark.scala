package models

import play.api.libs.json._

case class Landmark(id: Long, name: String, description: String,
	lat: Double, long: Double)

/**
 * Companion object for Landmark class. 
 */
object Landmark {
  implicit val landmarkFormat = Json.format[Landmark]
}
