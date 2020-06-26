package openh.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import openh.PrettyPrint
import openh.model.OpenHours
import JsonFormats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

/**
 * Declares routing of pretty print REST API
 *
 * @author Anton Revkov
 */
trait PrettyPrintApi {

  def prettyPrintApi(implicit system: ActorSystem): Route = {
    (post & path("api" / "v1" / "prettyprint")) {
      entity(as[OpenHours]) { hours =>
        // bad design to reply here with plain text (instead of json)
        // but that's what the task asks
        complete(PrettyPrint.openHours(hours))
      }
    }
  }

}
