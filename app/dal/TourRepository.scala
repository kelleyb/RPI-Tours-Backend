package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.Tour

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for tours.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this 
 * for you.
 */
@Singleton
class TourRepository @Inject()(
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
   * Here we define the table. It will have a name of "tours" within the 
   * database
   */
  private class ToursTable(tag: Tag) 
      extends Table[Tour](tag, "tours") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column, can't be null */
    def name = column[String]("name")

    /** The description column, can't be null */
    def description = column[String]("description")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Tour object.
     */
    def * = (id, name, description) <> 
      ((Tour.apply _).tupled, Tour.unapply)
  }

  /**
   * The starting point for all queries on the tours table.
   */
  private val tours = TableQuery[ToursTable]

  /**
   * Create a tour with the given values.
   *
   * This is an asynchronous operation, it will return a future of the created 
   * tour, which can be used to obtain the
   * id for that tour.
   */
  def create(
      name: String, 
      description: String): Future[Tour] = db.run {
    // We create a projection of just the main columns, since 
    // we're not inserting a value for the id column
    (tours.map(t => (t.name, t.description))
      // Now define it to return the id, because we want to know what id was 
      // generated for the tour
      returning tours.map(_.id)
      // And we define a transformation for the returned value, which combines 
      // our original parameters with the returned id
      into ((tour, id) => 
        Tour(id, tour._1, tour._2))
    // And finally, insert the tour into the database
    ) += (name, description)
  }

  /**
   * List all the tours in the database.
   */
  def list(): Future[Seq[Tour]] = db.run {
    tours.result
  }

  /**
   * Find tour with corresponding id
   */
  def findById(id: Long): Future[Tour] = db.run {
    tours.filter(_.id === id).result.head
  }


}
