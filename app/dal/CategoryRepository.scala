package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import java.time.LocalDateTime

import models.Category

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for categories.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this 
 * for you.
 */
@Singleton
class CategoryRepository @Inject() (
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
   * Here we define the table. It will have a name of "categories" within the 
   * database
   */
  private class CategoriesTable(tag: Tag) 
      extends Table[Category](tag, "categories") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column, can't be null */
    def name = column[String]("name")

    /** The description column, can't be null */
    def description = column[String]("description")

    /** The last time the category was updated */
    def lastUpdated = column[String]("last_updated")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Category object.
     */
    def * = (id, name, description, lastUpdated) <> 
      ((Category.apply _).tupled, Category.unapply)
  }

  /**
   * The starting point for all queries on the categories table.
   */
  private val categories = TableQuery[CategoriesTable]

  /**
   * Create a category with the given values.
   *
   * This is an asynchronous operation, it will return a future of the created 
   * category, which can be used to obtain the
   * id for that category.
   */
  def create(name: String, description: String): Future[Category] = db.run {
    // We create a projection of just the main columns, since 
    // we're not inserting a value for the 
    // id column
    (categories.map(c => (c.name, c.description, c.lastUpdated))
      // Now define it to return the id, because we want to know what id was 
      // generated for the category
      returning categories.map(_.id)
      // And we define a transformation for the returned value, which combines 
      // our original parameters with the returned id
      into ((cat, id) => Category(id, cat._1, cat._2, cat._3))
    // And finally, insert the category into the database
    ) += (name, description, LocalDateTime.now().toString.replace('T', ' '))
  }

  /**
   * List all the categories in the database.
   */
  def list(): Future[Seq[Category]] = db.run {
    categories.result
  }

  /**
   * Find category with corresponding id
   */
  def findById(id: Long): Future[Option[Category]] = db.run {
    categories.filter(_.id === id).result.headOption
  }

  /**
   * Update the given categories' timestamp
   */
  def updateTimestamp(id: Long) = db.run {
    val tStamp = for {
      cat <- categories if cat.id === id
    } yield cat.lastUpdated

    // Update the timestamp, make sure it's in a consistent format
    tStamp.update(LocalDateTime.now().toString.replace('T', ' '))

  }

}
