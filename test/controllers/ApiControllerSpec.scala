package controllers

import org.scalatestplus.play._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Environment, Configuration }
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.libs.json.JsArray
import play.api.Mode

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class ApiControllerSpec extends PlaySpec {

  // Current API endpoints
  // Whenever a new endpoint is added, we should test it.
  // GET /api/v1/tours                              - Complete
  // GET /api/v1/tours/:id                          - Complete
  // GET /api/v1/categories                         - Complete
  // GET /api/v1/categories/:id                     - Incomplete
  // GET /api/v1/categories/:id/tours               - Incomplete
  // GET /api/v1/tours/:id/last_updated             - Incomplete
  // GET /api/v1/categories/:id/last_updated        - Incomplete

  implicit val app = new GuiceApplicationBuilder()
    .configure(
        Configuration.from(
            Map(
                "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
                "slick.dbs.default.db.driver" -> "org.h2.Driver",
                "slick.dbs.default.db.url" -> "jdbc:h2:mem:play;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=FALSE",
                "play.evolutions.autoApply" -> "true",
                "play.evolutions.autoApplyDowns" -> "true",
                "play.evolutions.db.default.autoApply" -> "true",
                "play.evolutions.db.default.autoApplyDowns" -> "true",
                "slick.default" -> "models.*"
            )
        )
    )
    .in(Mode.Test)
    .build()

  def apiController(implicit app: Application) = {
    val app2ApiController = Application.instanceCache[ApiController]
    app2ApiController(app)
  }


  "ApiController GET /api/v1/tours" should {

    "return valid JSON, succeed" in {
      val tours = apiController.getTours().apply(FakeRequest())

      status(tours) mustBe OK
      contentType(tours) mustBe Some("application/json")
      (contentAsJson(tours) \ "status").as[String] mustBe "success"
    }

    "contain a single element by default" in {
      val tours = apiController.getTours().apply(FakeRequest())

      status(tours) mustBe OK
      contentType(tours) mustBe Some("application/json")
      (contentAsJson(tours) \ "content").as[JsArray].value.length mustBe 1
    }

    "match the sample JSON" in {
      val tours = apiController.getTours().apply(FakeRequest())

      status(tours) mustBe OK
      contentType(tours) mustBe Some("application/json")
      Json.parse(
        // replace any instances of a DateTime format with "time"
        Constants.DateTime.replaceAllIn(contentAsString(tours), "time")
      ) mustBe Constants.allToursJson
    }
  }


  "ApiController GET /api/v1/tours/:id" should {
    "return valid JSON, succeed when the tours exists" in {
      val tour = apiController.getTour(1).apply(FakeRequest())

      status(tour) mustBe OK
      contentType(tour) mustBe Some("application/json")
      (contentAsJson(tour) \ "status").as[String] mustBe "success"
    }

    "match the sample JSON" in {
      val tour = apiController.getTour(1).apply(FakeRequest())

      status(tour) mustBe OK
      contentType(tour) mustBe Some("application/json")
      Json.parse(
        // replace any instances of a DateTime format with "time"
        Constants.DateTime.replaceAllIn(contentAsString(tour), "time")
      ) mustBe Json.obj(
        ("content" -> (Constants.allToursJson \ "content")(0).as[JsObject])
      ) ++ apiController.successCode
    }
  }


  "ApiController GET /api/v1/categories" should {
    "return valid JSON, succeed" in {
      val categories = apiController.getCategories().apply(FakeRequest())

      status(categories) mustBe OK
      contentType(categories) mustBe Some("application/json")
      (contentAsJson(categories) \ "status").as[String] mustBe "success"
    }

    "contain a single element by default" in {
      val categories = apiController.getCategories().apply(FakeRequest())

      status(categories) mustBe OK
      contentType(categories) mustBe Some("application/json")
      (contentAsJson(categories) \ "content").as[JsArray].value.length mustBe 1
    }

    "match the sample JSON" in {
      val categories = apiController.getCategories().apply(FakeRequest())

      status(categories) mustBe OK
      contentType(categories) mustBe Some("application/json")
      Json.parse(
        // replace any instances of a DateTime format with "time"
        Constants.DateTime.replaceAllIn(contentAsString(categories), "time")
      ) mustBe Constants.allCategoriesJson
    }
  }


}


object Constants {
  val dateRegex = """([0-9]{4}-[0-9]{2}-[0-9]{2})"""
  val timeRegex = """([0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3})"""
  val DateTime = (dateRegex + " " + timeRegex).r

  val allToursJson = Json.parse("""{
    "content":[
      {
        "id":1,
        "name":"Freshman Residence Halls",
        "description":"Hey! This is a tour of the freshman residence halls!",
        "lastUpdated":"time",
        "waypoints":[
          {
            "id":1,
            "lat":42.73064179,
            "long":73.67553949,
            "tourId":1,
            "ordering":0
          },
          {
            "id":2,
            "lat":42.72898,
            "long":-73.67414,
            "tourId":1,
            "ordering":1
          },
          {
            "id":3,
            "lat":42.72848,
            "long":-73.67455,
            "tourId":1,
            "ordering":2
          }
        ],
        "landmarks":[
          {
            "id":1,
            "name":"RPI Admissions",
            "description":"This is the admissions building.",
            "lat":42.73064179,
            "long":-73.67553949,
            "photos":[
              {
                "id":1,
                "url":"http://www.example.com/image.jpg"
              }
            ]
          },
          {
            "id":2,
            "name":"Barton Residence Hall",
            "description":"Barton was first opened in Fall 2000 and in addition to being our newest residence hall, it also has the distinction of being the campus' only freshman only residence hall.",
            "lat":42.73064179,
            "long":-73.67553949,
            "photos":[
              {
                "id":2,
                "url":"http://www.example.com/image.jpg"
              }
            ]
          },
          {
            "id":3,
            "name":"Commons Dining Hall",
            "description":"Situated at the center of the first-year residence halls, the Commons offers several dining stations. These include the popular Asian Pacifica, a savory grill program, pasta prepared to order, a deli and Theme Cuisine.",
            "lat":42.73064179,
            "long":-73.67553949,
            "photos":[
              {
                "id":3,
                "url":"http://www.example.com/image.jpg"
              }
            ]
          }
        ]
      }
    ],
    "status":"success"
  }""")

  val allCategoriesJson = Json.parse("""{
    "content":[
      {
        "id":1,
        "name":"General Tours",
        "description":"This section consists of four athletic tours that go around campus!",
        "lastUpdated":"time",
        "numAvailableTours":1
      }
    ],
    "status":"success"
  }""")

}