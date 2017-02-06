package models

import play.api.libs.json._

case class Photo(id: Long, url: String)

/**
 * Companion object for Photo class. 
 */
object Photo {
  implicit val photoFormat = Json.format[Photo]
}
