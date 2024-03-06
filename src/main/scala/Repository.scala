import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime, LocalDateTime}

class Repository(db: Database)(implicit ec: ExecutionContext) {
    def getAll(): Future[Seq[Movie]] = db.run(SlickTables.movietable.result)
    def getById(id: Long): Future[Option[Movie]] = db.run(SlickTables.movietable.filter(_.id === id).result.headOption)
    def getShowtimesById(movieId: Long): Future[Seq[ShowTime]] = db.run(SlickTables.showtimetable.filter(_.movieId === movieId).result)
    def getShowtimeById(showtimeId: Long): Future[Option[ShowTime]] = db.run(SlickTables.showtimetable.filter(_.id === showtimeId).result.headOption)
    def getReservationById(reservationId: Long): Future[Option[Reservation]]= db.run(SlickTables.reservationtable.filter(_.id === reservationId).result.headOption)
    def mkReservation(showtimeId: Long): Future[Int] = db.run(SlickTables.reservationtable += Reservation(1L, showtimeId, false, false))
    def updateReservation(showtimeId: Long, increment: Boolean): Future[Int] = {
        val delta = if (increment) 1 else -1
        val query = for (showtime <- SlickTables.showtimetable if showtime.id === showtimeId) yield showtime.reservations
        val action = query.result.head.flatMap{x => query.update(x + delta)}
        db.run(action)
    }
    def updatePenalty(reservationId: Long, apply: Boolean): Future[Int] = {
        val action = SlickTables.reservationtable
            .filter(_.id === reservationId)
            .map(r => (r.canceled, r.penalty))
            .update((true, apply))
        db.run(action)
    }
    def populate(): Future[Unit] = {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val five = LocalDateTime.now().plusHours(5)
        val fiveDate = five.format(dateFormatter)
        val fiveTime = five.format(timeFormatter)
        val forty = LocalDateTime.now().plusHours(40)
        val fortyDate = forty.format(dateFormatter)
        val fortyTime = forty.format(timeFormatter)

        val dataQuery = DBIO.seq(
            SlickTables.movietable += Movie(1L, "Oppenheimer"),
            SlickTables.movietable += Movie(2L, "Barbie"),
            SlickTables.showtimetable += ShowTime(1L, 1L, fiveDate, fiveTime, 10, 2),
            SlickTables.showtimetable += ShowTime(2L, 1L, fortyDate, fortyTime, 10, 10),
            SlickTables.showtimetable += ShowTime(3L, 2L, fiveDate, fiveTime, 10, 8),
            SlickTables.showtimetable += ShowTime(4L, 2L, fortyDate, fortyTime, 10, 3),
            SlickTables.reservationtable += Reservation(1L, 3L, false, false),
            SlickTables.reservationtable += Reservation(2L, 4L, false, false),
            SlickTables.reservationtable += Reservation(3L, 3L, false, false)
        )
        db.run(dataQuery.transactionally).map(_ => println("database was populated with sameple data"))
    }
}