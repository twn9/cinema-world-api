import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import slick.jdbc.PostgresProfile.api._

object Routes extends JsonSupport {
    def route(logic: Logic)(implicit system: ActorSystem): Route = {
        // curl localhost:8080/health
        path("health") {
            get {
                complete(StatusCodes.OK)
            }
        } ~
        path("movies") {
            get {
                onComplete(logic.getAllMovies()){
                    case Success(movies) => complete(StatusCodes.OK, movies)
                    case Failure(ex) => complete(StatusCodes.InternalServerError, ex)
                }
            }
        } ~
        path("movies" / LongNumber) { id =>
            get {
                onComplete(logic.getMovie(id)){
                    case Success(Some(movieDetail)) => complete(StatusCodes.OK, movieDetail)
                    case Success(None) => complete(StatusCodes.NotFound, "error: Movie not found")
                    case Failure(ex) => complete(StatusCodes.InternalServerError, ex)
                }
            } 
        } ~
        path("movies" / LongNumber / "showtimes") { id =>
            get {
                onComplete(logic.getShowtimes(id)){
                    case Success(showTimes) => 
                        if (showTimes.nonEmpty){
                            complete(StatusCodes.OK, showTimes)
                        } 
                        else {
                            complete(StatusCodes.NotFound, "error: Showtime or Movie not found")
                        }
                    case Failure(ex) => complete(StatusCodes.InternalServerError, ex)
                }
            }   
        } ~
        // curl -X POST localhost:8080/reserce/{"id"}
        path("reserve" / LongNumber) { id =>
            post {
                onComplete(logic.reserve(id)){
                    case Success(true) => complete(StatusCodes.OK, "Reserved")
                    case Success(false) => complete(StatusCodes.NotFound, "Could not reserve due to insufficient seating or no avaliable showtime")
                    case Failure(ex) => complete(StatusCodes.InternalServerError, ex)
                }
            }
        } ~
        // curl -X PUT localhost:8080/cancel/{"id"}
        path("cancel" / LongNumber) { id =>
            put {
                onComplete(logic.cancel(id)){
                    case Success(x) => complete(StatusCodes.OK, x)
                    case Failure(ex) => complete(StatusCodes.InternalServerError, ex)
                }
            }
        }
    }
}