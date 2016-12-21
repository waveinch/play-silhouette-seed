package providers

import ch.wavein.play.mongo.providers.Provider
import models.AuthToken
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Created by unoedx on 20/12/16.
 */
trait AuthTokenProvider extends Provider[AuthToken] {
  def findByToken(token: String): Future[Option[AuthToken]]
  def findExpired(date: DateTime): Future[Seq[AuthToken]]
}
