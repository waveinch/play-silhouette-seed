package models

import java.util.UUID

import ch.wavein.play.mongo.model.Identity
import org.joda.time.DateTime

/**
 * A token to authenticate a user against an endpoint for a short time period.
 *
 * @param _id The unique token ID.
 * @param userID The unique ID of the user the token is associated with.
 * @param expiry The date-time the token expires.
 */
case class AuthToken(
  _id: Option[String],
  token: String,
  userID: String,
  expiry: DateTime) extends Identity
