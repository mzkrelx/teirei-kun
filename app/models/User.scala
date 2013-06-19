package models

object GitHubUser extends Enumeration {
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

case class SessionUser (
  id: String,
  name: String)