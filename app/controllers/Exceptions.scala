package controllers

case class BadParameterException(msg: String) extends Exception(msg)

