package openh.model

object TimeType extends Enumeration {
  type TypeType = Value
  val open, close = Value
}

/**
 * @param `type` takes values: 'open' or 'close'
 * @param value  unix date time - 00:00 = 0, 23:59 = 86340
 * @author Anton Revkov
 */
case class OpenCloseTime(`type`: TimeType.TypeType, value: Int) {

  /** @return true if it's an open hour (minutes 'r welcomed as well, but anyway) */
  def isOpen: Boolean = `type` == TimeType.open

}

/**
 * Defines restaurant open hours.
 *
 * Note:
 * <ul>
 * <li>it's possible not to pass a day at all</li>
 * <li>it's possible to empty set of working hours</li>
 * <li>order of working hours isn't defined</li>
 * </ul>
 *
 * @author Anton Revkov
 */
case class OpenHours(
                      monday   : Option[Seq[OpenCloseTime]] = None,
                      tuesday  : Option[Seq[OpenCloseTime]] = None,
                      wednesday: Option[Seq[OpenCloseTime]] = None,
                      thursday : Option[Seq[OpenCloseTime]] = None,
                      friday   : Option[Seq[OpenCloseTime]] = None,
                      saturday : Option[Seq[OpenCloseTime]] = None,
                      sunday   : Option[Seq[OpenCloseTime]] = None
                    )
