package controllers

import play.api.mvc._
import models.GitHubUser

trait Secured {

  private def username(request: RequestHeader) = request.session.get("username")

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.showSignIn)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withAuthUrlEncoded(f: => String => Request[Map[String,Seq[String]]] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action.apply(BodyParsers.parse.urlFormEncoded) { request =>
        f(user)(request)
      }
    }
  }

  def withUser(f: GitHubUser => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    // 'username' is id
    GitHubUser.findByID(username.toLong).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }

  def withUserUrlEncoded(f: GitHubUser => Request[Map[String,Seq[String]]] => Result) = withAuthUrlEncoded { username => implicit request =>
    // 'username' is id
    GitHubUser.findByID(username.toLong).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }
}
