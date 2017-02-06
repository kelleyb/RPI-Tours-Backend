package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.TourWaypoint

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for tourWaypoint values. These map Tours to Waypoints
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this 
 * for you.
 */
@Singleton
class TourWaypointRepository @Inject()(
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
   * Here we define the table. It will have a name of "tourWaypoints" within the 
   * database
   */
  private class TourWaypointsTable(tag: Tag) 
      extends Table[TourWaypoint](tag, "tourWaypoints") {

    /** The tourId column, can't be null */
    def tourId = column[Long]("tour_id")
    
    /** The waypointId column, can't be null */
    def waypointId = column[Long]("waypoint_id")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the TourWaypoint object.
     */
    def * = (tourId, waypointId) <> 
      ((TourWaypoint.apply _).tupled, TourWaypoint.unapply)
  }

  /**
   * The starting point for all queries on the tourWaypoints table.
   */
  private val tourWaypoints = TableQuery[TourWaypointsTable]

  /**
   * Create a tourWaypoint with the given values.
   *
   * This is an asynchronous operation, it will return a future of the created 
   * tourWaypoint, which can be used to obtain the
   * id for that tourWaypoint.
   */
  def create(
      tourId: Long,
      waypointId: Long): Future[TourWaypoint] = db.run {

    // For some reason, I wasn't able to do just something like 
    // tourWaypoints += TourWaypoint(tourId, waypointId), so I had to make
    // this cancer. This is how I got rid of the compiler errors.
    (tourWaypoints.map(tw => (tw.tourId, tw.waypointId))
      returning tourWaypoints.map(_.tourId)
      into ((tw, tw2) => 
        TourWaypoint(tw._1, tw._2))
    ) += (tourId, waypointId)
  }

  /**
   * List all the tourWaypoints in the database.
   */
  def list(): Future[Seq[TourWaypoint]] = db.run {
    tourWaypoints.result
  }
}
