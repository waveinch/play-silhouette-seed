package controllers.auth

import javax.inject.Inject

import _root_.services.{ AuthTokenService, UserService }
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers._
import controllers.{ WebJarAssets }
import forms.SignUpForm
import models.User
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc.Controller
import utils.auth.DefaultEnv

import scala.concurrent.Future

/**
 * The `Sign Up` controller.
 *
 * @param messagesApi            The Play messages API.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param authTokenService       The auth token service implementation.
 * @param avatarService          The avatar service implementation.
 * @param passwordHasherRegistry The password hasher registry.
 * @param mailerClient           The mailer client.
 * @param webJarAssets           The webjar assets implementation.
 */
class SignUpController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
  passwordHasherRegistry: PasswordHasherRegistry,
  mailerClient: MailerClient,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  /**
   * Views the `Sign Up` page.
   *
   * @return The result to display.
   */
  def view = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.auth.signUp(SignUpForm.form)))
  }

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.signUp(form))),
      data => {
        val result = Redirect(routes.SignUpController.view()).flashing("info" -> Messages("sign.up.email.sent", data.email))
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            val url = routes.SignInController.view().absoluteURL()
            mailerClient.send(Email(
              subject = Messages("email.already.signed.up.subject"),
              from = Messages("email.from"),
              to = Seq(data.email),
              bodyText = Some(views.txt.auth.emails.alreadySignedUp(user, url).body),
              bodyHtml = Some(views.html.auth.emails.alreadySignedUp(user, url).body)
            ))

            Future.successful(result)
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(data.password)
            val user = User(
              None,
              loginInfo = loginInfo,
              firstName = Some(data.firstName),
              lastName = Some(data.lastName),
              fullName = Some(data.firstName + " " + data.lastName),
              email = Some(data.email),
              avatarURL = None,
              activated = false
            )
            for {
              avatar <- avatarService.retrieveURL(data.email)
              user <- userService.save(user.copy(avatarURL = avatar))
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authToken <- authTokenService.create(user.identity)
            } yield {
              val url = routes.ActivateAccountController.activate(authToken.token).absoluteURL()
              mailerClient.send(Email(
                subject = Messages("email.sign.up.subject"),
                from = Messages("email.from"),
                to = Seq(data.email),
                bodyText = Some(views.txt.auth.emails.signUp(user, url).body),
                bodyHtml = Some(views.html.auth.emails.signUp(user, url).body)
              ))

              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              result
            }
        }
      }
    )
  }
}
