package openh

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import openh.model.TimeType._
import openh.model.{OpenCloseTime, OpenHours}
import openh.rest.PrettyPrintApi
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class RestSpec extends WordSpec
  with Matchers
  with ScalaFutures
  with ScalatestRouteTest
  with PrettyPrintApi {

  // use the json formats to marshal and unmarshall objects in the test

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import openh.rest.JsonFormats._

  "OpenHoursServer" should {

    "be able to pretty print open hours" in {

      val openHours = OpenHours(
        monday = Some(Seq(
          OpenCloseTime(open, 32400),
          OpenCloseTime(close, 72000)
        ))
      )
      val entity = Marshal(openHours).to[MessageEntity].futureValue

      // using the RequestBuilding DSL:
      val request = Post("/api/v1/prettyprint").withEntity(entity)

      request ~> prettyPrintApi ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be a plain text:
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)

        // and we know what message we're expecting back:
        entityAs[String] should ===(
          """A restaurant is open:
            |Monday: 9 AM - 8 PM""".stripMargin)
      }

    }

    "return Bad Request on unclosed working interval" in {

      val openHours = OpenHours(
        monday = Some(Seq(
          OpenCloseTime(open, 32400)
        ))
      )
      val entity = Marshal(openHours).to[MessageEntity].futureValue

      // using the RequestBuilding DSL:
      val request = Post("/api/v1/prettyprint").withEntity(entity)

      request ~> prettyPrintApi ~> check {
        // not 400 since custom error handler wraps it on a upper layer
        status should ===(StatusCodes.InternalServerError)

        // we expect the response to be a plain text:
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("There was an internal server error.")
      }

    }

  }

}
