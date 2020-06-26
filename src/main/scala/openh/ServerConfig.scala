package openh

import com.typesafe.config.Config

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
 * Holds all server configs typed and ready to be mixed-in
 * in any part of the app
 *
 * @author Anton Revkov
 */
trait ServerConfig {

  val config: Config

  /** Note: Config.getDuration returns non-finite duration */
  def timeout(duration: String): FiniteDuration = {
    Duration(duration).asInstanceOf[FiniteDuration]
  }

  lazy val restBind    = config.getString("rest.bind")
  lazy val restPort    = config.getInt("rest.port")
  lazy val restTimeout = timeout(config.getString("rest.timeout"))

  // feel free to expand

}
