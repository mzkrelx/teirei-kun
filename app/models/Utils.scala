package models

import play.api.Play

object Utils {

  def playConfig = Play.configuration(Play.current)

}