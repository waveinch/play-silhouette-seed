package providers.impl

import javax.inject.Inject

import ch.wavein.play.mongo.providers.MongoProvider
import models.{ AuthToken, Formatters }
import org.joda.time.DateTime
import play.api.libs.json.{ Json, OFormat }
import play.modules.reactivemongo.ReactiveMongoApi
import providers.AuthTokenProvider
import play.modules.reactivemongo.json._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by unoedx on 20/12/16.
 */
class AuthTokenMongo @Inject() (val reactiveMongoApi: ReactiveMongoApi)(implicit val ec: ExecutionContext) extends AuthTokenProvider with MongoProvider[AuthToken] {

  override def collectionName: String = Collections.authToken

  override implicit def formatter: OFormat[AuthToken] = Formatters.authTokenF

  override def findByToken(token: String): Future[Option[AuthToken]] = for {
    col <- collection
    auth <- col.find(Json.obj("token" -> token)).one[AuthToken]
  } yield auth

  override def findExpired(date: DateTime): Future[Seq[AuthToken]] = for {
    col <- collection
    expired <- col.find(Json.obj("expiry" -> Json.obj("$lt" -> date.getMillis))).cursor[AuthToken]().collect[Seq]()
  } yield expired
}