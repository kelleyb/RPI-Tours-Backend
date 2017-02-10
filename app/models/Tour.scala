package models

import play.api.libs.json._

case class Tour(
    id: Long, 
    name: String, 
    description: String, 
    lastUpdated: String)

/**
 * Companion object for Tour class. 
 */
object Tour {
  implicit val tourFormat = Json.format[Tour]
}
