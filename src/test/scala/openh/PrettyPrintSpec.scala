package openh

import openh.model.TimeType._
import openh.model.{OpenCloseTime, OpenHours}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FreeSpec, Matchers}

/**
 * @author Anton Revkov
 */
class PrettyPrintSpec extends FreeSpec with Matchers with TableDrivenPropertyChecks {

  "format time" in {
    forAll(Table(
      "time" -> "format",
      0L     -> "12 AM",
      3600L  -> "1 AM",
      3660L  -> "1:01 AM",
      32400L -> "9 AM",
      39600L -> "11 AM",
      57600L -> "4 PM",
      82800L -> "11 PM",
      86399L -> "11:59 PM"
      //86400 - INVALID VALUE
    )) { (time, expected) =>
      PrettyPrint.formatTime(time) shouldBe expected
    }
  }

  "open hours" - {

    "task case #1" in {
      val openHours = OpenHours(
        monday = Some(Seq(
          OpenCloseTime(open, 32400),
          OpenCloseTime(close, 72000))
        )
      )
      PrettyPrint.openHours(openHours, None) shouldBe "Monday: 9 AM - 8 PM"
    }

    "task case #2" in {
      val openHours = OpenHours(
        friday = Some(Seq(
          OpenCloseTime(open, 64800)
        )),
        saturday = Some(Seq(
          OpenCloseTime(close, 3600),
          OpenCloseTime(open, 32400),
          OpenCloseTime(close, 39600),
          OpenCloseTime(open, 57600),
          OpenCloseTime(close, 82800),
        ))
      )
      PrettyPrint.openHours(openHours) shouldBe
        """A restaurant is open:
          |Friday: 6 PM - 1 AM
          |Saturday: 9 AM - 11 AM, 4 PM - 11 PM""".stripMargin
    }

    "24/7 except Monday" in {
      val openHours = OpenHours(
        monday = Some(Seq(
          OpenCloseTime(close, 1000),
          OpenCloseTime(open, 80000))
        )
      )
      PrettyPrint.openHours(openHours, None) shouldBe "Monday: 10:13 PM - 12:16 AM"
    }

    "weekend only" in {
      val openHours = OpenHours(
        friday = Some(Seq(
          OpenCloseTime(open, 18 * 60 * 60))
        ),
        monday = Some(Seq(
          OpenCloseTime(close, 3 * 60 * 60))
        )
      )
      PrettyPrint.openHours(openHours, None) shouldBe "Friday: 6 PM - 3 AM"
    }

    "no open hours" in {
      PrettyPrint.openHours(OpenHours()) shouldBe "A restaurant is open:\n"
      PrettyPrint.openHours(OpenHours(friday = Some(Seq.empty))) shouldBe "A restaurant is open:\n"
    }

    "exception on open interval" in {
      val openHours = OpenHours(
        friday = Some(Seq(
          OpenCloseTime(open, 3600)
        ))
      )
      intercept[IllegalArgumentException](PrettyPrint.openHours(openHours, None)).getMessage shouldBe
        "Not even number of open/close hours for the day: FRIDAY"
    }

    "exception on unclosed internval" in {
      val openHours = OpenHours(
        friday = Some(Seq(
          OpenCloseTime(open, 3600),
          OpenCloseTime(open, 36000)
        ))
      )
      intercept[IllegalArgumentException](PrettyPrint.openHours(openHours, None)).getMessage shouldBe
        "Expected close time: 36000 [Friday]"
    }

  }

}
