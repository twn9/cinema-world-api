import scala.concurrent.Future
import scala.concurrent.ExecutionContextExecutor
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime, LocalDateTime}
import scala.concurrent.ExecutionContext

import slick.jdbc.PostgresProfile.api._
import java.time.ZoneId

class Logic(db: Database)(implicit ec: ExecutionContext){


    def getAll(): Future[Seq[Movie]] = {
        db.run(SlickTables.movietable.result)
    }


    def getMovie(id: Long): Future[Option[Movie]] = {
        db.run(SlickTables.movietable.filter(_.id === id).result.headOption)
    }


    def getShowtimes(movieId: Long): Future[Seq[ShowTime]] = {
        db.run(SlickTables.showtimetable.filter(_.movieId === movieId).result)
    }


    def reserve(id: Long): Future[Boolean] = {
        val maxQuery = SlickTables.showtimetable.filter(_.id === id).map(_.max).result.headOption
        val resQuery = SlickTables.showtimetable.filter(_.id === id).map(_.reservations).result.headOption

        val action = for {
            m <- db.run(maxQuery)
            r <- db.run(resQuery)
            result <- (m, r) match {
                case (Some(max), Some(reservations)) if reservations < max =>
                    val update = DBIO.seq(
                        SlickTables.showtimetable.filter(_.id === id).map(_.reservations).update(reservations + 1),
                        SlickTables.reservationtable += Reservation(1L, id, 0.0f)
                    )
                    db.run(update.transactionally).map(_ => true)
                case _ =>
                    Future.successful(false)
            }
        } yield result

        action
    }


    def cancel(id: Long): Future[String] = {
        val showIdQuery = SlickTables.reservationtable.filter(_.id === id).map(_.showtimeId).result.headOption

        val cancellationResult = for {
            showIdOption <- db.run(showIdQuery)
            result <- showIdOption match {
            case Some(showId) =>
                val showtimeQuery = SlickTables.showtimetable.filter(_.id === showId).map(s => (s.id, s.date, s.time, s.reservations)).result.headOption
                for {
                    showtimeOption <- db.run(showtimeQuery)
                    cancellationResult <- showtimeOption match {
                        case Some((showId, dateString, timeString, currentReservations)) =>
                            val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
                            val showtimeDateTime = LocalDateTime.of(date, time)
                            
                            val currentDateTime = LocalDateTime.now(ZoneId.systemDefault())
                            val duration = java.time.Duration.between(currentDateTime, showtimeDateTime)
                            
                            if (duration.toHours < 24 && duration.toHours > 0) {
                                val update = DBIO.seq(
                                    SlickTables.showtimetable.filter(_.id === showId).map(_.reservations).update(currentReservations - 1),
                                    SlickTables.reservationtable.filter(_.id === id).map(_.penalty).update(0.8f)
                                )
                                db.run(update.transactionally).map(_ => "Canceled with penalty")
                            } else {
                                val update = DBIO.seq(
                                    SlickTables.showtimetable.filter(_.id === showId).map(_.reservations).update(currentReservations - 1),
                                    SlickTables.reservationtable.filter(_.id === id).map(_.penalty).update(1.0f)
                                )
                                db.run(update.transactionally).map(_ => "Canceled")
                            }
                        case None => Future.successful("Showtime not found")
                    }
                } yield cancellationResult
            case None => Future.successful("Reservation not found")
            }
        } yield result

        cancellationResult
    }

    def populate(): Future[Unit] = {
        db.run(SlickTables.movietable.filter(_.title === "Barbie").exists.result).flatMap { exists =>
            if (exists){
                Future.successful(println("database is already populated with sameple data"))
            } 
            else {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

                val fiveHours = LocalDateTime.now().plusHours(5)
                val fiveHoursDate = fiveHours.format(dateFormatter)
                val fiveHoursTime = fiveHours.format(timeFormatter)
                val fortyHours = LocalDateTime.now().plusHours(40)
                val fortyHoursDate = fortyHours.format(dateFormatter)
                val fortyHoursTime = fortyHours.format(timeFormatter)

                val dataQuery = DBIO.seq(
                    SlickTables.movietable += Movie(1L, "Oppenheimer"),
                    SlickTables.movietable += Movie(2L, "Barbie"),
                    SlickTables.showtimetable += ShowTime(1L, 1L, fiveHoursDate, fiveHoursTime, 10, 2),
                    SlickTables.showtimetable += ShowTime(2L, 1L, fortyHoursDate, fortyHoursTime, 10, 10),
                    SlickTables.showtimetable += ShowTime(3L, 2L, fiveHoursDate, fiveHoursTime, 10, 8),
                    SlickTables.showtimetable += ShowTime(4L, 2L, fortyHoursDate, fortyHoursTime, 10, 3),
                    SlickTables.reservationtable += Reservation(1L, 3L, 0.0f),
                    SlickTables.reservationtable += Reservation(2L, 4L, 0.0f),
                    SlickTables.reservationtable += Reservation(3L, 3L, 0.0f)
                )

                db.run(dataQuery.transactionally).map(_ => println("database was populated with sameple data"))
            }

        }
    }
}