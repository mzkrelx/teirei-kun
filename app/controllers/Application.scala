package controllers

import scala.collection.JavaConverters.mapAsJavaMapConverter
import models.GitHubUser
import models.GitHubUser
import models.Utils.playConfig
import play.api.Logger
import play.api.Routes
import play.api.mvc.Action
import play.api.mvc.Controller
import play.libs.Json.toJson
import play.libs.WS
import java.net.URL
import models.GitHubUserAPI

object Application extends Controller {

  /** Sign in or Program list if logged in */
  def index = Programs.listPrograms

  def showSignIn() = Action {
    Ok(views.html.signinform(playConfig.getString("oauth.github.client_id").get))
  }

  def testLogin() = Action {
    val user = GitHubUser(1111111111, "loginId", "name", "email",
        new URL("https://secure.gravatar.com/avatar/3a722bee9a12f1a32b4b5493cd48ad05?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png"))
    Logger.info("GitHubUser=" + user)

    GitHubUser.saveOrUpdate(user)

    Ok(views.html.signinsuccess(user)).withSession(
      "username" -> user.id.toString)

  }

  def callBackGitHub(code: String) = Action { implicit request =>

    val postBodyMap = Map(
      "client_id"     -> toJson(playConfig.getString("oauth.github.client_id").get),
      "client_secret" -> toJson(playConfig.getString("oauth.github.client_secret").get),
      "code"          -> toJson(code)
    ).asJava

    val postBodyJson = toJson(postBodyMap)
    Logger.debug("postBody=" + postBodyJson)

    val responce = WS.url("https://github.com/login/oauth/access_token")
      .setHeader("Accept", "application/json")
      .post(postBodyJson);
    Logger.debug("response=" + responce.get.asJson)

    val accessToken = responce.get.asJson.get("access_token").asText
    Logger.debug("accessToken=" + accessToken)

    val user = WS.url("https://api.github.com/user")
      .setQueryParameter("access_token", accessToken)
      .get
      .get.asJson
    Logger.debug("userJson=" + user)

    val githubUser = GitHubUser(
      user.get(GitHubUserAPI.ID.toString).asLong,
      user.get(GitHubUserAPI.Login.toString).asText,
      user.get(GitHubUserAPI.Name.toString).asText,
      user.get(GitHubUserAPI.Email.toString).asText,
      new URL(user.get(GitHubUserAPI.AvatarURL.toString).asText))
    Logger.info("GitHubUser=" + githubUser)

    GitHubUser.saveOrUpdate(githubUser)

    Ok(views.html.signinsuccess(githubUser)).withSession(
      "username" -> githubUser.id.toString)
       // TODO  show program list page
  }

  def signOut = Action {
    Redirect(routes.Application.index).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRouter", Some("jQuery.ajax"))(
      routes.javascript.Attendances.updateAttendance,
      routes.javascript.Attendances.deleteAttendance)
    ).as("text/javascript")
  }

}
