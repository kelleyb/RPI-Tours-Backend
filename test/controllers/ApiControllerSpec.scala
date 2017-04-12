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
 * Test the API controller, make sure all the endpoints return the expected 
 * values
 */
class ApiControllerSpec extends PlaySpec {

  // Current API endpoints
  // Whenever a new endpoint is added, we should test it.
  // GET /api/v1/tours                              - Complete
  // GET /api/v1/tours/:id                          - Complete
  // GET /api/v1/categories                         - Complete
  // GET /api/v1/categories/:id                     - Complete
  // GET /api/v1/categories/:id/tours               - Complete
  // GET /api/v1/tours/:id/last_updated             - Complete
  // GET /api/v1/categories/:id/last_updated        - Complete

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


  "GET /api/v1/tours" should {

    "return valid JSON, succeed" in {
      val tours = apiController.getTours().apply(FakeRequest())

      status(tours) mustBe OK
      contentType(tours) mustBe Some("application/json")
      (contentAsJson(tours) \ "status").as[String] mustBe "success"
      contentAsJson(tours).as[JsObject].keys.contains("content") mustBe true
    }

    "contain two elements by default" in {
      val tours = apiController.getTours().apply(FakeRequest())

      status(tours) mustBe OK
      contentType(tours) mustBe Some("application/json")
      (contentAsJson(tours) \ "content").as[JsArray].value.length mustBe 2
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


  "GET /api/v1/tours/:id" should {
    "return valid JSON, succeed when the tour exists" in {
      val tour = apiController.getTour(1).apply(FakeRequest())

      status(tour) mustBe OK
      contentType(tour) mustBe Some("application/json")
      (contentAsJson(tour) \ "status").as[String] mustBe "success"
      contentAsJson(tour).as[JsObject].keys.contains("content") mustBe true
    }

    "return valid JSON, fail with 404 when the tour does not exist" in {
      val tour = apiController.getTour(-1).apply(FakeRequest())

      status(tour) mustBe NOT_FOUND
      contentType(tour) mustBe Some("application/json")
      (contentAsJson(tour) \ "status").as[String] mustBe "failure"
      contentAsJson(tour).as[JsObject].keys.contains("error") mustBe true
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

  "GET /api/v1/tours/:id/last_updated" should {
    "return valid JSON, succeed when the tour exists" in {
      val updated = apiController.getTimeTourLastUpdated(1).apply(FakeRequest())

      status(updated) mustBe OK
      contentType(updated) mustBe Some("application/json")
      (contentAsJson(updated) \ "status").as[String] mustBe "success"
      contentAsJson(updated).as[JsObject].keys.contains("content") mustBe true
    }

    "return valid JSON, fail with 404 when the tour does not exist" in {
      val updated = apiController.getTimeTourLastUpdated(-1).apply(FakeRequest())

      status(updated) mustBe NOT_FOUND
      contentType(updated) mustBe Some("application/json")
      (contentAsJson(updated) \ "status").as[String] mustBe "failure"
      contentAsJson(updated).as[JsObject].keys.contains("error") mustBe true
    }

    "match the sample JSON" in {
      val updated = apiController.getTimeTourLastUpdated(1).apply(FakeRequest())

      status(updated) mustBe OK
      contentType(updated) mustBe Some("application/json")
      Json.parse(
        // replace any instances of a DateTime format with "time"
        Constants.DateTime.replaceAllIn(contentAsString(updated), "time")
      ) mustBe Constants.lastUpdatedJson
    }
  }


  "GET /api/v1/categories" should {
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

  "GET /api/v1/categories/:id" should {
    "return valid JSON, succeed when the category exists" in {
      val category = apiController.getCategory(1).apply(FakeRequest())

      status(category) mustBe OK
      contentType(category) mustBe Some("application/json")
      (contentAsJson(category) \ "status").as[String] mustBe "success"
      contentAsJson(category).as[JsObject].keys.contains("content") mustBe true
    }

    "return valid JSON, fail with 404 when the category does not exist" in {
      val category = apiController.getCategory(-1).apply(FakeRequest())

      status(category) mustBe NOT_FOUND
      contentType(category) mustBe Some("application/json")
      (contentAsJson(category) \ "status").as[String] mustBe "failure"
      contentAsJson(category).as[JsObject].keys.contains("error") mustBe true
    }

    "match the sample JSON" in {
      val category = apiController.getCategory(1).apply(FakeRequest())

      status(category) mustBe OK
      contentType(category) mustBe Some("application/json")
      Json.parse(
        // replace any instances of a DateTime format with "time"
        Constants.DateTime.replaceAllIn(contentAsString(category), "time")
      ) mustBe Json.obj(
        ("content" -> (Constants.allCategoriesJson \ "content")(0).as[JsObject])
      ) ++ apiController.successCode
    }
  }

  "GET /api/v1/categories/:id/tours" should {
    "return valid JSON, succeed when the category exists" in {
      val tours = apiController.getToursForCategory(1).apply(FakeRequest())

      status(tours) mustBe OK
      contentType(tours) mustBe Some("application/json")
      (contentAsJson(tours) \ "status").as[String] mustBe "success"
      contentAsJson(tours).as[JsObject].keys.contains("content") mustBe true
    }

    "return valid JSON, fail with 404 when the category does not exist" in {
      val tours = apiController.getToursForCategory(-1).apply(FakeRequest())

      status(tours) mustBe NOT_FOUND
      contentType(tours) mustBe Some("application/json")
      (contentAsJson(tours) \ "status").as[String] mustBe "failure"
      contentAsJson(tours).as[JsObject].keys.contains("error") mustBe true
    }

    "match the sample JSON" in {
      val tours = apiController.getToursForCategory(1).apply(FakeRequest())

      status(tours) mustBe OK
      contentType(tours) mustBe Some("application/json")
      Json.parse(
        // replace any instances of a DateTime format with "time"
        Constants.DateTime.replaceAllIn(contentAsString(tours), "time")
      ) mustBe Constants.cat1ToursJson
    }
  }

  "GET /api/v1/categories/:id/last_updated" should {
    "return valid JSON, succeed when the category exists" in {
      val updated = apiController.getTimeCatLastUpdated(1).apply(FakeRequest())

      status(updated) mustBe OK
      contentType(updated) mustBe Some("application/json")
      (contentAsJson(updated) \ "status").as[String] mustBe "success"
      contentAsJson(updated).as[JsObject].keys.contains("content") mustBe true
    }

    "return valid JSON, fail with 404 when the category does not exist" in {
      val updated = apiController.getTimeCatLastUpdated(-1).apply(FakeRequest())

      status(updated) mustBe NOT_FOUND
      contentType(updated) mustBe Some("application/json")
      (contentAsJson(updated) \ "status").as[String] mustBe "failure"
      contentAsJson(updated).as[JsObject].keys.contains("error") mustBe true
    }

    "match the sample JSON" in {
      val updated = apiController.getTimeCatLastUpdated(1).apply(FakeRequest())

      status(updated) mustBe OK
      contentType(updated) mustBe Some("application/json")
      Json.parse(
        // replace any instances of a DateTime format with "time"
        Constants.DateTime.replaceAllIn(contentAsString(updated), "time")
      ) mustBe Constants.lastUpdatedJson
    }
  }

  "GET /api/v1/categories_last_updated" should {
    "return valid JSON, succeed" in {
      val updated = apiController.getTimeAllCatsLastUpdated.apply(FakeRequest())

      status(updated) mustBe OK
      contentType(updated) mustBe Some("application/json")
      (contentAsJson(updated) \ "status").as[String] mustBe "success"
      contentAsJson(updated).as[JsObject].keys.contains("content") mustBe true
    }

    "match the sample JSON" in {
      val updated = apiController.getTimeAllCatsLastUpdated.apply(FakeRequest())

      status(updated) mustBe OK
      contentType(updated) mustBe Some("application/json")
      Json.parse(
        // replace any instances of a DateTime format with "time"
        Constants.DateTime.replaceAllIn(contentAsString(updated), "time")
      ) mustBe Constants.catsLastUpdatedJson
    }
  }
}


object Constants {
  val dateRegex = """([0-9]{4}-[0-9]{2}-[0-9]{2})"""
  val timeRegex = """([0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]*)"""
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
            "long":-73.67553949,
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
            "lat":42.72898,
            "long":-73.67414,
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
            "lat":42.72848,
            "long":-73.67455,
            "photos":[
              {
                "id":3,
                "url":"http://www.example.com/image.jpg"
              }
            ]
          }
        ]
      },
      {
        "id":2,
        "name":"Second Tour",
        "description":"This tour shouldn't show up if you're getting tours for a specific category! It's unmapped!",
        "lastUpdated":"time",
        "waypoints":[

        ],
        "landmarks":[

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

  val catsLastUpdatedJson = Json.parse("""{
    "content":[
      {
        "category_id":1,
        "last_updated":"time"
      }
    ],
    "status":"success"
  }""")

  val cat1ToursJson = Json.parse("""{
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
            "long":-73.67553949,
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
            "lat":42.72898,
            "long":-73.67414,
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
            "lat":42.72848,
            "long":-73.67455,
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

  val lastUpdatedJson = Json.parse("""{
    "content":{
      "lastUpdated":"time"
    },
    "status":"success"
  }""")

}