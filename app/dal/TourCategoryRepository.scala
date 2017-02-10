package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.TourCategory

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for tourCategory values. These map Tours to Categories
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this 
 * for you.
 */
@Singleton
class TourCategoryRepository @Inject()(
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
   * Here we define the table. It will have a name of "tourCategories" within the 
   * database
   */
  private class TourCategoriesTable(tag: Tag) 
      extends Table[TourCategory](tag, "tour_categories") {

    /** The tourId column, can't be null */
    def tourId = column[Long]("tour_id")
    
    /** The categoryId column, can't be null */
    def categoryId = column[Long]("category_id")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the TourCategory object.
     */
    def * = (tourId, categoryId) <> 
      ((TourCategory.apply _).tupled, TourCategory.unapply)
  }

  /**
   * The starting point for all queries on the tourCategories table.
   */
  private val tourCategories = TableQuery[TourCategoriesTable]

  /**
   * Create a tourCategory with the given values.
   *
   * This is an asynchronous operation, it will return a future of the created 
   * tourCategory, which can be used to obtain the
   * id for that tourCategory.
   */
  def create(
      tourId: Long,
      categoryId: Long): Future[TourCategory] = db.run {

    // For some reason, I wasn't able to do just something like 
    // tourCategories += TourCategory(tourId, categoryId), so I had to make
    // this cancer. This is how I got rid of the compiler errors.
    (tourCategories.map(tc => (tc.tourId, tc.categoryId))
      returning tourCategories.map(_.tourId)
      into ((tc, tc2) => 
        TourCategory(tc._1, tc._2))
    ) += (tourId, categoryId)
  }

  /**
   * List all the tourCategories in the database.
   */
  def list(): Future[Seq[TourCategory]] = db.run {
    tourCategories.result
  }

  /**
   * Return all TourCategories with the corresponding tourId
   */
  def findByTourId(id: Long): Future[Seq[Long]] = db.run {
    tourCategories.filter(_.tourId === id).map(_.categoryId).result
  }

  /**
   * Return all TourCategories with the corresponding categoryId
   */
  def findByCategoryId(id: Long): Future[Seq[Long]] = db.run {
    tourCategories.filter(_.categoryId === id).map(_.tourId).result
  }
}
