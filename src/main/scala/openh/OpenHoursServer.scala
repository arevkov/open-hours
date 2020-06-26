package openh

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, ExceptionHandler}
import com.typesafe.config.{Config, ConfigFactory}
import openh.rest.PrettyPrintApi

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

/**
 * @author Anton Revkov
 */
class OpenHoursServer(val config: Config) extends ServerConfig
  with Directives
  with PrettyPrintApi {

  def start(): Unit = {

    implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName, config)

    // worker threads (as opposed to selector and acceptor threads)
    implicit val executor: ExecutionContextExecutor = system.dispatcher

    // TODO: some exceptions are definitely worth to be classified as internal
    // others are just a result of input data validation (Bad Request),
    // but might appear in a variety of flavours
    // thoroughly consider hierarchy of errors
    implicit def errHandler: ExceptionHandler = ExceptionHandler {
      // in case of invalid input data we should better return 400 code
      // and skip logging (expected behavior)
      case e: IllegalArgumentException =>
        complete(HttpResponse(StatusCodes.BadRequest, entity = e.getMessage))
    }

    val restApi = withRequestTimeout(restTimeout) {
      prettyPrintApi
      // ~ anotherApi
    }

    Http()
      .bindAndHandle(restApi, restBind, restPort)
      .onComplete {
        case Success(binding) =>
          val address = binding.localAddress
          system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
        case Failure(ex) =>
          system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
          system.terminate()
      }

  }

}

object OpenHoursServer {

  def main(args: Array[String]): Unit = {
    // might want to override some configs w/ program parameters here (scopt)
    new OpenHoursServer(ConfigFactory.load()).start()
  }

}
