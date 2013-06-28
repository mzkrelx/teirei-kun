package models

import java.net.URL

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current



object GitHubUserAPI extends Enumeration {
  val Login             = Value("login")
  val ID                = Value("id")
  val AvatarURL         = Value("avatar_url")
  val GravatarID        = Value("gravatar_id")
  val URL               = Value("url")
  val HtmlURL           = Value("html_url")
  val FollowersURL      = Value("followers_url")
  val FollowingURL      = Value("following_url")
  val GistsURL          = Value("gists_url")
  val StarredURL        = Value("starred_url")
  val SubscriptionsURL  = Value("subscriptions_url")
  val OrganizationsURL  = Value("organizations_url")
  val ReposURL          = Value("repos_url")
  val EventsURL         = Value("events_url")
  val ReceivedEventsURL = Value("received_events_url")
  val Type              = Value("type")
  val Name              = Value("name")
  val Company           = Value("company")
  val Blog              = Value("blog")
  val Location          = Value("location")
  val Email             = Value("email")
  val Hireable          = Value("hireable")
  val Bio               = Value("bio")
  val PublicRepos       = Value("public_repos")
  val Followers         = Value("followers")
  val Following         = Value("following")
  val CreatedAt         = Value("created_at")
  val UpdatedAt         = Value("updated_at")
  val PublicGists       = Value("public_gists")
}

object GitHubUser {

  def saveOrUpdate(user: GitHubUser) {
    DB.withConnection { implicit c =>
      val rows = SQL("""
        SELECT *
          FROM github_user g
          WHERE id = {github_user_id}""")
        .on('github_user_id -> user.id)()

      rows match {
        case r if (r.length == 1) => SQL("""
          UPDATE github_user SET
            login      = {login_id},
            name       = {name},
            email      = {email},
            avatar_url = {avatar_url}
          WHERE
            id         = {id}""")
          .on('login_id   -> user.loginID,
              'name       -> user.name,
              'email      -> user.email,
              'avatar_url -> user.avatarURL.toString,
              'id         -> user.id)
          .executeUpdate()
        case _ => SQL("""
          INSERT INTO github_user
            VALUES (
              {id},
              {login_id},
              {name},
              {email},
              {avatar_url})""")
          .on('id         -> user.id,
              'login_id   -> user.loginID,
              'name       -> user.name,
              'email      -> user.email,
              'avatar_url -> user.avatarURL.toString)
          .executeInsert()
      }
    }
  }

  def findByID(id: Long): Option[GitHubUser] = {
    DB.withConnection { implicit c =>
      val rows = SQL("""
        SELECT *
          FROM github_user g
          WHERE id = {id}""")
        .on('id -> id)()

      rows.headOption map { row =>
        GitHubUser(
          row[Pk[Long]]("id").get,
          row[String]("login"),
          row[String]("name"),
          row[String]("email"),
          new URL(row[String]("avatar_url"))
        )
      }
    }
  }

}

case class GitHubUser (
  id:        Long,
  loginID:   String,
  name:      String,
  email:     String,
  avatarURL: URL)
