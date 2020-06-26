package openh

import java.time.DayOfWeek._
import java.time.LocalTime
import java.time.format.{DateTimeFormatter, TextStyle}
import java.util.Locale

import openh.model.OpenHours

/**
 * Holds APIs related to representing an info in an
 * human-readable perhaps shorten form
 *
 * @author Anton Revkov
 */
object PrettyPrint {

  private val SecondsInHour   = 60 * 60
  private val Clock12ShortFmt = DateTimeFormatter.ofPattern("h a")
  private val Clock12FullFmt  = DateTimeFormatter.ofPattern("h:mm a")

  /**
   * Since the task example exposes no minutes,
   * but at the same time the task doesn't restrict to open/close in the mid of an hour,
   * let's have that fuzzy formatter (didn't find a way to make minutes optional in DateTimeFormatter)
   *
   * @param unixTime number of seconds since the beginning of the day
   */
  def formatTime(unixTime: Long): String = {
    if (unixTime < 0 || unixTime >= 24 * 60 * 60) {
      throw new IllegalArgumentException("Unix time expected in a range: [0, 86400)")
    }
    val time = LocalTime.ofSecondOfDay(unixTime)
    // omit printing minutes if it's the beginning of an hour
    val format = if (unixTime % SecondsInHour == 0) {
      Clock12ShortFmt
    } else {
      Clock12FullFmt
    }
    time.format(format)
  }

  /**
   * Given restaurant working hours, print them in human-readable format
   * NOTE: we assume that day order is important in an output
   *
   * @return example:
   * {{{
   * A restaurant is open:
   * Friday: 6 PM - 1 AM
   * Saturday: 9 AM -11 AM, 4 PM - 11 PM
   * }}}
   */
  def openHours(hours: OpenHours,
                prefix: Option[String] = Some("A restaurant is open:")
               ): String = {
    // let's keep days of week in order
    // @formatter:off
    val week = Seq(
      MONDAY    -> hours.monday,
      TUESDAY   -> hours.tuesday,
      WEDNESDAY -> hours.wednesday,
      THURSDAY  -> hours.thursday,
      FRIDAY    -> hours.friday,
      SATURDAY  -> hours.saturday,
      SUNDAY    -> hours.sunday
    )
    // @formatter:on

    val refined = week
      // drop off undefined days or the days w/ empty provided working hours
      // (no need to pretty print them)
      .filter { case (_, hours) => hours.exists(_.nonEmpty) }
      // let's not assume that open hours will be provided in sorted order
      // (we can require it, but it's better to be tolerant here)
      .map { case (day, Some(hours)) => day -> hours.sortBy(_.value) }

    // if a restaurant doesn't have any open hours - just return empty result
    val pp = if (refined.isEmpty) "" else {
      // append the head to the tail to get close hour from the next day
      // example1: opens Sunday 11 PM, closes Monday 3 AM
      // example2: opens Monday 8 PM, closes Monday 1 AM (24/7 except Monday :)
      val cyclic = (refined :+ refined.head).sliding(2, 1)

      // group open-close hours into intervals
      // NOTE: the end of an interval might be located in the next day
      val intervals = cyclic.flatMap {
        case (day, overnightYstMb :: hours) :: (_, overnightMaybe :: _) :: Nil =>
          // whether or not it's a closing hour of previous day
          val head = if (overnightYstMb.isOpen) Seq(overnightYstMb) else Seq.empty
          // whether or not it's a closing hour of given day
          val tail = if (overnightMaybe.isOpen) Seq.empty else Seq(overnightMaybe)

          val withOvernight = head ++ hours ++ tail
          if (withOvernight.isEmpty) {
            // NOTE: here it can turn out that it's not a work day
            // and holds only a close hour to the previous day (expected input)
            None
          } else if (withOvernight.size % 2 == 0) {
            Some(day -> withOvernight.sliding(2, 2))
          } else {
            throw new IllegalArgumentException(s"Not even number of open/close hours for the day: ${day.name()}")
          }
      }

      // print intervals
      intervals.map {
        case (day, intraday) =>
          // in a camel-case style
          val dayName = {day.getDisplayName(TextStyle.FULL, Locale.US)}
          // aggregate all day working hours inline
          val format12HourClock = intraday.map {
            case open :: close :: Nil =>
              // validate that close hour is actually a close hour
              if (!open.isOpen) throw new IllegalArgumentException(s"Expected open time: ${open.value} [$dayName]")
              if (close.isOpen) throw new IllegalArgumentException(s"Expected close time: ${close.value} [$dayName]")
              s"${formatTime(open.value)} - ${formatTime(close.value)}"
          }.mkString(", ")
          // and finally the result for a day
          s"$dayName: $format12HourClock"
      }.mkString("\n")

    }

    // prepend open hours w/ prefix (if any)
    prefix.foldLeft(pp) {
      case (pp, prefix) => s"$prefix\n$pp"
    }

  }

}
