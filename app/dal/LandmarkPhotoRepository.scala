package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.LandmarkPhoto

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for landmarkPhoto values. These map Photos to Landmarks
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this 
 * for you.
 */
@Singleton
class LandmarkPhotoRepository @Inject()(
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
   * Here we define the table. It will have a name of "landmarkPhotos" within the 
   * database
   */
  private class LandmarkPhotosTable(tag: Tag) 
      extends Table[LandmarkPhoto](tag, "landmark_photos") {
    
    /** The landmarkId column, can't be null */
    def landmarkId = column[Long]("landmark_id")

    /** The photoId column, can't be null */
    def photoId = column[Long]("photo_id")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the LandmarkPhoto object.
     */
    def * = (landmarkId, photoId) <> 
      ((LandmarkPhoto.apply _).tupled, LandmarkPhoto.unapply)
  }

  /**
   * The starting point for all queries on the landmarkPhotos table.
   */
  private val landmarkPhotos = TableQuery[LandmarkPhotosTable]

  /**
   * Create a landmarkPhoto with the given values.
   *
   * This is an asynchronous operation, it will return a future of the created 
   * landmarkPhoto, which can be used to obtain the
   * id for that landmarkPhoto.
   */
  def create(
      photoId: Long,
      landmarkId: Long): Future[LandmarkPhoto] = db.run {

    // For some reason, I wasn't able to do just something like 
    // landmarkPhotos += LandmarkPhoto(photoId, landmarkId), so I had to make
    // this cancer. This is how I got rid of the compiler errors.
    (landmarkPhotos.map(tl => (tl.landmarkId, tl.photoId))
      returning landmarkPhotos.map(_.photoId)
      into ((tl, tl2) => 
        LandmarkPhoto(tl._1, tl._2))
    ) += (landmarkId, photoId)
  }

  /**
   * List all the landmarkPhotos in the database.
   */
  def list(): Future[Seq[LandmarkPhoto]] = db.run {
    landmarkPhotos.result
  }


}
