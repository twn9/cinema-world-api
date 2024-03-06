import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor
import slick.jdbc.PostgresProfile.api._
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime, LocalDateTime}
import java.time.ZoneId

class Logic(repository: Repository)(implicit ec: ExecutionContext){

    def getAllMovies(): Future[Seq[Movie]]= repository.getAll()

    def getMovie(id: Long): Future[Option[Movie]] = repository.getById(id)

    def getShowtimes(id: Long): Future[Seq[ShowTime]] = repository.getShowtimesById(id)

    def reserve(showtimeId: Long): Future[Boolean] = {
        repository.getShowtimeById(showtimeId).flatMap {
            case Some(showtime) if showtime.reservations < showtime.max =>
                for {
                    _ <- repository.updateReservation(showtimeId, true)
                    _ <- repository.mkReservation(showtimeId)
                } yield true
            case _ => Future.successful(false)
        }
    }

    def cancel(reservationId: Long): Future[String] = {
        repository.getReservationById(reservationId).flatMap {
            case Some(reservation) =>
                repository.getShowtimeById(reservation.showtimeId).flatMap {
                    case Some(showtime) =>
                        val date = LocalDate.parse(showtime.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val time = LocalTime.parse(showtime.time, DateTimeFormatter.ofPattern("HH:mm"))
                        val showtimeDateTime = LocalDateTime.of(date, time)
                        val currentDateTime = LocalDateTime.now(ZoneId.systemDefault())
                        val duration = java.time.Duration.between(currentDateTime, showtimeDateTime)
                        
                        if (duration.toHours < 24 && duration.toHours > 0) {
                            repository.updateReservation(showtime.id, false)
                            repository.updatePenalty(reservation.id, true).map(_ => "Canceled with penalty")
                        } else {
                            repository.updateReservation(showtime.id, false)
                            repository.updatePenalty(reservation.id, false).map(_ => "Canceled")
                        }
                    case _ => Future.successful("Showtime not found")
                }
            case None => Future.successful("Reservation not found")
        }
    }
}