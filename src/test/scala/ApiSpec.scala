import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class ApiSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with JsonSupport {
  val route = Routes.route(system)
  Thread.sleep(2000)

  "API" should {
    "return 200 status code Get/ health" in {
      Get("/health") ~> route ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "return all showing movies Get/ movies" in {
      Get("/movies") ~> route ~> check {
        status shouldBe StatusCodes.OK
        val movies = entityAs[Seq[Movie]]
        movies should not be empty
      }
    }

    "return movie info Get/ movies/{id}" in {
      Get("/movies/1") ~> route ~> check {
        status shouldBe StatusCodes.OK
        val info = entityAs[Movie]
        info.id shouldBe 1
      }
    }

    "return not found response Get/ movies/{id}" in {
      Get("/movies/99") ~> route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return a showtimes for a movie Get/ movie/{id}/showtimes" in {
      Get("/movies/1/showtimes") ~> route ~> check {
        status shouldBe StatusCodes.OK
        val times = entityAs[Seq[ShowTime]]
        times.foreach { time => time.movieId shouldBe 1 }
      }
    }
    
    "return a successful reservation Get/ reserve/{id}" in {
      Post("/reserve/1") ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual "Reserved"
      }
    }

    "return a not found response Get/ reserve/{id}" in {
      Post("/reserve/99") ~> route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return a successful cancelation Get/ cancel/{id}" in {
      Post("/cancel/3") ~> route ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "return a not found response Get/ cancel/{id}" in {
      Post("/cancel/99") ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldEqual "Reservation not found"
      }
    }

  }
}