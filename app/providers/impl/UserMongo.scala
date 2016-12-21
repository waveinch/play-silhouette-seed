package providers.impl

import javax.inject.Inject

import ch.wavein.play.mongo.providers.MongoProvider
import models.{ Formatters, User }
import play.api.libs.json.{ Json, OFormat }
import play.modules.reactivemongo.ReactiveMongoApi
import providers.UserProvider
import play.modules.reactivemongo.json._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by unoedx on 20/12/16.
 */
class UserMongo @Inject() (val reactiveMongoApi: ReactiveMongoApi)(implicit val ec: ExecutionContext) extends UserProvider with MongoProvider[User] {

  override def collectionName: String = Collections.user

  override implicit def formatter: OFormat[User] = Formatters.userF

  override def findByEmail(email: String): Future[Option[User]] = for {
    col <- collection
    user <- col.find(Json.obj("email" -> email)).one[User]
  } yield user
}
