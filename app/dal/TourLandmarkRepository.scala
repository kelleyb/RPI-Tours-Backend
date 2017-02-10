package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.TourLandmark

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for tourLandmark values. These map Tours to Landmarks
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this 
 * for you.
 */
@Singleton
class TourLandmarkRepository @Inject()(
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
   * Here we define the table. It will have a name of "tourLandmarks" within the 
   * database
   */
  private class TourLandmarksTable(tag: Tag) 
      extends Table[TourLandmark](tag, "tour_landmarks") {

    /** The tourId column, can't be null */
    def tourId = column[Long]("tour_id")
    
    /** The landmarkId column, can't be null */
    def landmarkId = column[Long]("landmark_id")

    /** 
     * The ordering column, can't be null 
     * We define this here because a landmark can be in many tours 
     */
    def ordering = column[Int]("ordering")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the TourLandmark object.
     */
    def * = (tourId, landmarkId, ordering) <> 
      ((TourLandmark.apply _).tupled, TourLandmark.unapply)
  }

  /**
   * The starting point for all queries on the tourLandmarks table.
   */
  private val tourLandmarks = TableQuery[TourLandmarksTable]

  /**
   * Create a tourLandmark with the given values.
   *
   * This is an asynchronous operation, it will return a future of the created 
   * tourLandmark, which can be used to obtain the
   * id for that tourLandmark.
   */
  def create(
      tourId: Long,
      landmarkId: Long,
      ordering: Int): Future[TourLandmark] = db.run {

    // For some reason, I wasn't able to do just something like 
    // tourLandmarks += TourLandmark(tourId, landmarkId), so I had to make
    // this cancer. This is how I got rid of the compiler errors.
    (tourLandmarks.map(tl => (tl.tourId, tl.landmarkId, tl.ordering))
      returning tourLandmarks.map(_.tourId)
      into ((tl, tl2) => 
        TourLandmark(tl._1, tl._2, tl._3))
    ) += (tourId, landmarkId, ordering)
  }

  /**
   * List all the tourLandmarks in the database.
   */
  def list(): Future[Seq[TourLandmark]] = db.run {
    tourLandmarks.result
  }

  /**
   * Find landmark IDs with corresponding tourId
   */
  def findByTourId(id: Long): Future[Seq[Long]] = db.run {
    tourLandmarks.filter(_.tourId === id).map(_.landmarkId).result
  }
}
