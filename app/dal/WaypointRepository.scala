package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.Waypoint

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for waypoints.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this 
 * for you.
 */
@Singleton
class WaypointRepository @Inject()(
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
   * Here we define the table. It will have a name of "waypoints" within the 
   * database
   */
  private class WaypointsTable(tag: Tag) 
      extends Table[Waypoint](tag, "waypoints") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The latitude column, can't be null */
    def lat = column[Double]("lat")
    
    /** The longitude column, can't be null */
    def long = column[Double]("long")

    /** 
     * The tourId column, can't be null 
     * Used as essentially a foreignkey, but instead of enforcing a mapping
     * on the database we simply store the related tour's ID. We can therefore
     * do something like tours.filter(_.id === waypoint.landmarkId), 
     * which is functionally the same.
     */
    def tourId = column[Long]("tour_id")
    def ordering = column[Int]("touring")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Waypoint object.
     */
    def * = (id, lat, long, tourId, ordering) <> 
      ((Waypoint.apply _).tupled, Waypoint.unapply)
  }

  /**
   * The starting point for all queries on the waypoints table.
   */
  private val waypoints = TableQuery[WaypointsTable]

  /**
   * Create a waypoint with the given values.
   *
   * This is an asynchronous operation, it will return a future of the created 
   * waypoint, which can be used to obtain the
   * id for that waypoint.
   */
  def create(
      lat: Double, 
      long: Double,
      tourId: Long,
      ordering: Int): Future[Waypoint] = db.run {
    // We create a projection of just the main columns, since 
    // we're not inserting a value for the id column
    (waypoints.map(w => (w.lat, w.long, w.tourId, w.ordering))
      // Now define it to return the id, because we want to know what id was 
      // generated for the waypoint
      returning waypoints.map(_.id)
      // And we define a transformation for the returned value, which combines 
      // our original parameters with the returned id
      into ((waypoint, id) => 
        Waypoint(id, waypoint._1, waypoint._2, waypoint._3, waypoint._4))
    // And finally, insert the waypoint into the database
    ) += (lat, long, tourId, ordering)
  }

  /**
   * List all the waypoints in the database.
   */
  def list(): Future[Seq[Waypoint]] = db.run {
    waypoints.result
  }
}
