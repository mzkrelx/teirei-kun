package controllers

import play.api.mvc._

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

}
