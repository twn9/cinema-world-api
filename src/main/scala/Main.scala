import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import scala.concurrent.ExecutionContextExecutor
import slick.jdbc.PostgresProfile.api._

// JSON (un)marshalling support
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val movieFormat: RootJsonFormat[Movie] = jsonFormat2(Movie)
  implicit val showTimeFormat: RootJsonFormat[ShowTime] = jsonFormat6(ShowTime)
  implicit val reservationFormat: RootJsonFormat[Reservation] = jsonFormat4(Reservation)
}

object Main extends App with JsonSupport {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  
  val db = Database.forConfig("dbConfig")
  val repository = new Repository(db)
  val logic = new Logic(repository)
  val route = Routes.route(logic)

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server online at http://localhost:8080/")

  // To stop the server, press Enter in the console
  scala.io.StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}