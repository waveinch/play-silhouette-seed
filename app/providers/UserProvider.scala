package providers

import ch.wavein.play.mongo.providers.Provider
import models.User

import scala.concurrent.Future

/**
 * Created by unoedx on 20/12/16.
 */
trait UserProvider extends Provider[User] {
  def findByEmail(email: String): Future[Option[User]]
}
