package models

import java.net.URL

object Auth {
  val clientID: String = "{自分のアプリのclient_id｝"
  val clientSecret: String = "{自分のアプリのclient_id_secret｝"
  val accessTokenAccessPoint: URL = new URL("https://github.com/login/oauth/access_token")
  val usersApiAccessPoint: URL = new URL("https://api.github.com/user");
}