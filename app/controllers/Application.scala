package controllers

import scala.collection.JavaConverters.mapAsJavaMapConverter

import models.GitHubUser
import models.Utils.playConfig
import play.api.Logger
import play.api.Routes
import play.api.mvc.Action
import play.api.mvc.Controller
import play.libs.Json.toJson
import play.libs.WS

object Application extends Controller {

  def index = showSignIn

  def showSignIn = Action { implicit request =>
    Ok(views.html.signinform(playConfig.getString("oauth.github.client_id").get))
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

    val userJson = WS.url("https://api.github.com/user")
      .setQueryParameter("access_token", accessToken)
      .get
      .get.asJson
    Logger.debug("userJson=" + userJson)

    val githubUser = GitHubUser.fromJson(userJson)
    Logger.info("GitHubUser=" + githubUser)

    GitHubUser.saveOrUpdate(githubUser)

    Redirect(routes.Programs.listPrograms).withSession(
      "githubID" -> githubUser.id.toString)
  }

  def signOut = Action {
    Redirect(routes.Application.showSignIn).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRouter", Some("jQuery.ajax"))(
      routes.javascript.Attendances.addAttendance,
      routes.javascript.Attendances.updateAttendance,
      routes.javascript.Attendances.deleteAttendance)
    ).as("text/javascript")
  }
}
