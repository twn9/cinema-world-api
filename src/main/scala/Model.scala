import slick.jdbc.PostgresProfile.api._
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import java.sql.Ref

case class Movie(id: Long, title: String)
case class ShowTime(id: Long, movieId: Long, date:String, time: String, max:Int, reservations:Int)
case class Reservation(id: Long, showtimeId: Long, canceled: Boolean, penalty: Boolean)

object SlickTables {

    class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies"), "Movie") {
        def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def title: Rep[String] = column[String]("title")
        
        def * : ProvenShape[Movie] = (id, title) <> (Movie.tupled, Movie.unapply)
    }

    class ShowTimeTable(tag: Tag) extends Table[ShowTime](tag, Some("movies"), "ShowTime") {
        def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def movieId: Rep[Long] = column[Long]("movieId")
        def date: Rep[String] = column[String]("date")
        def time: Rep[String] = column[String]("time")
        def max: Rep[Int] = column[Int]("max")
        def reservations: Rep[Int] = column[Int]("reservations")
        
        def * : ProvenShape[ShowTime] = (id, movieId, date, time, max, reservations) <> (ShowTime.tupled, ShowTime.unapply)
    }

    class ReservationTable(tag: Tag) extends Table[Reservation](tag, Some("movies"), "Reservation") {
        def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def showtimeId: Rep[Long] = column[Long]("showtimeId")
        def canceled: Rep[Boolean] = column[Boolean]("canceled")
        def penalty: Rep[Boolean] = column[Boolean]("penalty")
        
        def * : ProvenShape[Reservation] = (id, showtimeId, canceled ,penalty) <> (Reservation.tupled, Reservation.unapply)
    }

    lazy val movietable = TableQuery[MovieTable]
    lazy val showtimetable = TableQuery[ShowTimeTable]
    lazy val reservationtable = TableQuery[ReservationTable]
}
