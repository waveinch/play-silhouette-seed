package services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.util.Clock
import models.AuthToken
import org.joda.time.DateTimeZone
import providers.AuthTokenProvider

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Handles actions to auth tokens.
 *
 * @param authTokenProvider The auth token provider implementation.
 * @param clock The clock instance.
 */
class AuthTokenServiceImpl @Inject() (authTokenProvider: AuthTokenProvider, clock: Clock)(implicit ec: ExecutionContext) extends AuthTokenService {

  /**
   * Creates a new auth token and saves it in the backing store.
   *
   * @param userID The user ID for which the token should be created.
   * @param expiry The duration a token expires.
   * @return The saved auth token.
   */
  def create(userID: String, expiry: FiniteDuration = 5 minutes) = {
    val token = AuthToken(None, UUID.randomUUID().toString, userID, clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt))
    authTokenProvider.insert(token)
  }

  /**
   * Validates a token ID.
   *
   * @param id The token ID to validate.
   * @return The token if it's valid, None otherwise.
   */
  def validate(id: String): Future[Option[AuthToken]] = authTokenProvider.findByToken(id)

  /**
   * Cleans expired tokens.
   *
   * @return The list of deleted tokens.
   */
  def clean = authTokenProvider.findExpired(clock.now.withZone(DateTimeZone.UTC)).flatMap { tokens =>
    Future.sequence(tokens.map { token =>
      authTokenProvider.delete(token.identity)
    })
  }
}
