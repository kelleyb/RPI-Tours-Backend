package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.Photo

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for photos.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this 
 * for you.
 */
@Singleton
class PhotoRepository @Inject()(
    dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will
  // let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the 
  // table and other queries.
  import dbConfig._
  import driver.api._

  /**
   * Here we define the table. It will have a name of "photos" within the 
   * database
   */
  private class PhotosTable(tag: Tag) 
      extends Table[Photo](tag, "photos") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The url column, can't be null */
    def url = column[String]("url")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Photo object.
     */
    def * = (id, url) <> 
      ((Photo.apply _).tupled, Photo.unapply)
  }

  /**
   * The starting point for all queries on the photos table.
   */
  private val photos = TableQuery[PhotosTable]

  /**
   * Create a photo with the given values.
   *
   * This is an asynchronous operation, it will return a future of the created 
   * photo, which can be used to obtain the
   * id for that photo.
   */
  def create(
      url: String, 
      landmarkId: Long): Future[Photo] = db.run {
    // We create a projection of just the main columns, since 
    // we're not inserting a value for the id column
    (photos.map(p => (p.url))
      // Now define it to return the id, because we want to know what id was 
      // generated for the photo
      returning photos.map(_.id)
      // And we define a transformation for the returned value, which combines 
      // our original parameters with the returned id
      into ((url, id) => 
        Photo(id, url))
    // And finally, insert the photo into the database
    ) += (url)
  }

  /**
   * List all the photos in the database.
   */
  def list(): Future[Seq[Photo]] = db.run {
    photos.result
  }


}
