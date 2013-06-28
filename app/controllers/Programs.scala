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

object Programs extends Controller {

  val programForm: Form[Program] = Form(
    mapping(
      "title"       -> nonEmptyText,
      "description" -> text,
      "schedules"   -> nonEmptyText
    )(Program.fromForm)(Program.toForm)
  )

  def listPrograms() = Action {
    val programs = Program.findAll
    Ok(views.html.programs(programs))
  }

  def addProgram = Action { implicit request =>
    programForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(errors)),
      program => {
        val programId = Program.save(program)
        Redirect(routes.Programs.showProgram(programId))
      }
    )
  }

  def showProgram(id: Int) = Action {
    val program = Program.findById(id)
    val attendances = Attendance.findByProgramId(id)
    Logger.debug(attendances.map(_.person).toString)
    val persons = attendances.map(_.person).distinct.toList
    Logger.debug(persons.toString)
    Ok(views.html.schedule(program, attendances, persons))
  }

  def editProgram(id: Int) = Action {
    NotImplemented
  }

}