package openh.rest

import openh.model.{OpenCloseTime, OpenHours, TimeType}
import openh.utils.EnumJsonConverter
import spray.json.DefaultJsonProtocol

/**
 * Might worth to keep json parsers closer to corresponding routers
 *
 * @author Anton Revkov
 */
object JsonFormats  {

  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val timeTypeFormat      = new EnumJsonConverter(TimeType)
  implicit val openCloseTimeFormat = jsonFormat2(OpenCloseTime)
  implicit val openHoursFormat     = jsonFormat7(OpenHours)

}
