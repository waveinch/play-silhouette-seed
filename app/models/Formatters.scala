package models

import play.api.libs.json.Json

/**
 * Created by unoedx on 20/12/16.
 */
object Formatters {
  implicit def userF = Json.format[User]
  implicit def authTokenF = Json.format[AuthToken]
}
