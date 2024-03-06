import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime, LocalDateTime}

class LogicSpec extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {
  val repository = mock[Repository]
  val logic = new Logic(repository)

  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  val five = LocalDateTime.now().plusHours(5)
  val fiveDate = five.format(dateFormatter)
  val fiveTime = five.format(timeFormatter)
  val forty = LocalDateTime.now().plusHours(40)
  val fortyDate = forty.format(dateFormatter)
  val fortyTime = forty.format(timeFormatter)

  val movie1 = Movie(1L, "Oppenheimer")
  val movie2 = Movie(2L, "Barbie")
  val showtime1 = ShowTime(1L, 1L, fortyDate, fortyTime, 10, 2)
  val showtime2 = ShowTime(2L, 2L, fiveDate, fiveTime, 10, 10)
  val reservation1 = Reservation(1L, 1L, false, false)
  val reservation2 = Reservation(2L, 2L, false, false)

  "Logic" should {

    "getAll returns all showing movies" in {
      (repository.getAll _).expects().returning(Future.successful(Seq(movie1, movie2)))

      whenReady(logic.getAllMovies()) { result =>
        result shouldBe Seq(movie1, movie2)
      }
    }

    "getMovie returns a movie with id" in {
      (repository.getById _).expects(1L).returning(Future.successful(Some(movie1)))

      whenReady(logic.getMovie(1L)) { result =>
        result shouldBe Some(movie1)
      }
    }

    "getMovie returns None when id is not in db" in {
      (repository.getById _).expects(99L).returning(Future.successful(None))

      whenReady(logic.getMovie(99L)) { result =>
        result shouldBe None
      }
    }

    "getShowtimes returns showtimes for a movie with id" in {
      (repository.getShowtimesById _).expects(1L).returning(Future.successful(Seq(showtime1)))

      whenReady(logic.getShowtimes(1L)) { result =>
        result shouldBe Seq(showtime1)
      }
    }

    "getShowtimes returns empty when no showtimes for movie in db" in {
      (repository.getShowtimesById _).expects(99L).returning(Future.successful(Seq.empty))

      whenReady(logic.getShowtimes(99L)) { result =>
        result shouldBe Seq.empty
      }
    }

    "reserve returns true for when a reservation is made" in {
      (repository.getShowtimeById _).expects(1L).returning(Future.successful(Some(showtime1)))
      (repository.updateReservation _).expects(1L, true).returning(Future.successful(1))
      (repository.mkReservation _).expects(1L).returning(Future.successful(1))

      whenReady(logic.reserve(1L)) { result =>
        result shouldBe true
      }
    }

    "reserve returns false for when a showtime is fully booked" in {
      (repository.getShowtimeById _).expects(2L).returning(Future.successful(Some(showtime2)))

      whenReady(logic.reserve(2L)) { result =>
        result shouldBe false
      }
    }

    "cancel returns Canceled message" in {
      (repository.getReservationById _).expects(1L).returning(Future.successful(Some(reservation1)))
      (repository.getShowtimeById _).expects(1L).returning(Future.successful(Some(showtime1)))
      (repository.updateReservation _).expects(1L, false).returning(Future.successful(1))
      (repository.updatePenalty _).expects(1L, false). returning(Future.successful(1))
      
      whenReady(logic.cancel(1L)) { result =>
        result shouldBe "Canceled"
      }
    }

    "cancel returns Canceled with penalty message" in {
      (repository.getReservationById _).expects(2L).returning(Future.successful(Some(reservation2)))
      (repository.getShowtimeById _).expects(2L).returning(Future.successful(Some(showtime2)))
      (repository.updateReservation _).expects(2L, false).returning(Future.successful(1))
      (repository.updatePenalty _).expects(2L, true). returning(Future.successful(1))
      
      whenReady(logic.cancel(2L)) { result =>
        result shouldBe "Canceled with penalty"
      }
    }
  }
}
