package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.Landmark

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for landmarks.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this 
 * for you.
 */
@Singleton
class LandmarkRepository @Inject()(
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
   * Here we define the table. It will have a name of "landmarks" within the 
   * database
   */
  private class LandmarksTable(tag: Tag) 
      extends Table[Landmark](tag, "landmarks") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column, can't be null */
    def name = column[String]("name")

    /** The description column, can't be null */
    def description = column[String]("description")

    /** The latitude column, can't be null */
    def lat = column[Double]("lat")

    /** The longitude column, can't be null */
    def long = column[Double]("long")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Landmark object.
     */
    def * = (id, name, description, lat, long) <> 
      ((Landmark.apply _).tupled, Landmark.unapply)
  }

  /**
   * The starting point for all queries on the landmarks table.
   */
  private val landmarks = TableQuery[LandmarksTable]

  /**
   * Create a landmark with the given values.
   *
   * This is an asynchronous operation, it will return a future of the created 
   * landmark, which can be used to obtain the
   * id for that landmark.
   */
  def create(
      name: String, 
      description: String, 
      lat: Double, 
      long: Double): Future[Landmark] = db.run {
    // We create a projection of just the name and description columns, since 
    // we're not inserting a value for the id column
    (landmarks.map(l => (l.name, l.description, l.lat, l.long))
      // Now define it to return the id, because we want to know what id was 
      // generated for the landmark
      returning landmarks.map(_.id)
      // And we define a transformation for the returned value, which combines 
      // our original parameters with the returned id
      into ((landmark, id) => 
        Landmark(id, landmark._1, landmark._2, landmark._3, landmark._4))
    // And finally, insert the landmark into the database
    ) += (name, description, lat, long)
  }

  /**
   * List all the landmarks in the database.
   */
  def list(): Future[Seq[Landmark]] = db.run {
    landmarks.result
  }


}
