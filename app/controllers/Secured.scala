package controllers

import play.api.mvc._
import models.GitHubUser

trait Secured {

  private def githubID(request: RequestHeader) = request.session.get("githubID")

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.showSignIn)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(githubID, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withAuthUrlEncoded(f: => String => Request[Map[String,Seq[String]]] => Result) = {
    Security.Authenticated(githubID, onUnauthorized) { user =>
      Action.apply(BodyParsers.parse.urlFormEncoded) { request =>
        f(user)(request)
      }
    }
  }

  def withUser(f: GitHubUser => Request[AnyContent] => Result) = withAuth { githubID => implicit request =>
    GitHubUser.findByID(githubID.toLong).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }

  def withUserUrlEncoded(f: GitHubUser => Request[Map[String,Seq[String]]] => Result) = withAuthUrlEncoded { githubID => implicit request =>
    GitHubUser.findByID(githubID.toLong).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }
}
