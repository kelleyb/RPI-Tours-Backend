package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import java.time.LocalDateTime

import models.Tour

import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{Success, Failure}


/**
 * A repository for tours.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this 
 * for you.
 */
@Singleton
class TourRepository @Inject()(
    dbConfigProvider: DatabaseConfigProvider,
    categories: CategoryRepository,
    tourCategories: TourCategoryRepository)(
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

    /** The last time the tour was updated */
    def lastUpdated = column[String]("last_updated")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Tour object.
     */
    def * = (id, name, description, lastUpdated) <> 
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
    (tours.map(t => (t.name, t.description, t.lastUpdated))
      // Now define it to return the id, because we want to know what id was 
      // generated for the tour
      returning tours.map(_.id)
      // And we define a transformation for the returned value, which combines 
      // our original parameters with the returned id
      into ((tour, id) => 
        Tour(id, tour._1, tour._2, tour._3))
    // And finally, insert the tour into the database
    ) += (name, description, LocalDateTime.now().toString.replace('T', ' '))
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

  /**
   * Update the given tour's timestamp
   */
  def updateTimestamp(id: Long) = {
    // We want to also update any categories this tour is a part of
    tourCategories.findByTourId(id) onComplete {
      case Success(catIds) => catIds.map { catId =>
        categories.updateTimestamp(catId)
      }
      case Failure(err) => println("An error occurred: " + err.getMessage)
    }

    db.run {
      val tStamp = for {
        t <- tours if t.id === id
      } yield t.lastUpdated

      // Update the timestamp, make sure it's in a consistent format
      tStamp.update(LocalDateTime.now().toString.replace('T', ' '))

    }
  }
}
