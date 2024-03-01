import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Await
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import akka.actor.ActorSystem


class LogicSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val db = Database.forConfig("dbConfig")
  val logic = new Logic(db)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(logic.populate(), 10.seconds)
  }

  "Logic.getAll()" should {
    "return all movies" in {
      val result = Await.result(logic.getAll(), 10.seconds)
      result should not be empty
      result.head.title shouldBe "Oppenheimer"
    }
  }

  "Logic.getMovie(id)" should {
    "return movie info using id" in {
      val id = 1L
      val result = Await.result(logic.getMovie(id), 10.seconds)
      result should not be empty
      result.head.id shouldBe 1
    }

    "return None for a false id" in {
      val id = 99L
      val result = Await.result(logic.getMovie(id), 10.seconds)
      result shouldBe None
    }
  }

  "Logic.getShowtimes(moiveId)" should {
    "return showtimes using id" in {
      val id = 1L
      val result = Await.result(logic.getShowtimes(id), 10.seconds)
      result should not be empty
      result.foreach { time => time.movieId shouldBe id }
    }

    "return None for a movie without showtimes" in {
      val id = 99L
      val result = Await.result(logic.getShowtimes(id), 10.seconds)
      result shouldBe empty
    }
  }

  "Logic.reserve(showTimeId)" should {
    "return true for reservation" in {
      val id = 1L
      val result = Await.result(logic.reserve(id), 10.seconds)
      result shouldBe true
    }

    "return false for reservation that is fully booked" in {
      val id = 2L 
      val result = Await.result(logic.reserve(id), 10.seconds)
      result shouldBe false
    }

    "return false for non existing showId" in {
      val id = 99L
      val result = Await.result(logic.reserve(id), 10.seconds)
      result shouldBe false
    }
  }

  "Logic.cancel(reservationId)" should {
    "return canceled with penalty" in {
      val id = 1L
      val result = Await.result(logic.cancel(id), 10.seconds)
      result shouldBe "Canceled with penalty"
    }

    "return canceled without penalty" in {
      val id = 2L
      Await.result(logic.reserve(id), 10.seconds)
      val result = Await.result(logic.cancel(id), 10.seconds)
      result shouldBe "Canceled"
    }

    "return Reservation not found for non exisitng reservationId" in {
      val id = 99L
      val result = Await.result(logic.cancel(id), 10.seconds)
      result shouldBe "Reservation not found"
    }
  }
}
