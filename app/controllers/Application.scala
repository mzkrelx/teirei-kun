package controllers

import anorm.Id
import anorm.NotAssigned
import anorm.Pk
import models.AttendChoice
import models.Attendance
import models.GitHubUser
import models.Person
import models.Program
import models.Schedule
import models.SessionUser
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import play.libs.Json
import play.libs.Json._
import play.libs.WS
import play.api.libs.json.JsNull
import collection.JavaConverters._
import models.GitHubUser
import play.api.libs.json.JsValue

object Application extends Controller {

  def index = Action {
    Ok(views.html.index(Programs.programForm))
  }

  def showSignIn() = Action {
    Ok(views.html.signinform())
  }

  def callBackGitHub(code: String) = Action { implicit request =>

    val playConfig = Play.configuration(Play.current)

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

    val sessionUser = SessionUser(
      user.get(GitHubUser.ID.toString).asText,
      user.get(GitHubUser.Name.toString).asText)
    Logger.info("sessionUser=" + sessionUser)

    Ok(views.html.signinsuccess(sessionUser))
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRouter", Some("jQuery.ajax"))(
      routes.javascript.Attendances.updateAttendance,
      routes.javascript.Attendances.deleteAttendance)
    ).as("text/javascript")
  }

}