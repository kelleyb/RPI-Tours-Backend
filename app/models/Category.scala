package models

import play.api.libs.json._

case class Category(
  id: Long, 
  name: String, 
  description: String, 
  lastUpdated: String)

/**
 * Companion object for Category class. 
 */
object Category {
  implicit val categoryFormat = Json.format[Category]
}
