package controllers

import models.Attendance
import models.Person
import models.Program
import models.Schedule
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import anorm.NotAssigned
import models.AttendChoice

class BadParameterException(msg: String) extends Exception(msg)

object Application extends Controller {

  val programForm: Form[Program] = Form(
    mapping(
      "title"       -> nonEmptyText,
      "description" -> text,
      "schedules"   -> nonEmptyText
    )(Program.fromForm)(Program.toForm)
  )

  def index = Action {
    Ok(views.html.index(programForm))
  }

  def listPrograms() = TODO

  def addProgram = Action { implicit request =>
    programForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(errors)),
      program => {
        Program.save(program)
        Ok("プログラムを作りました。")
      }
    )
  }

  def showProgram(id: Int) = Action {
    val program = Program.findById(id)
    val attendances = Attendance.findByProgramId(id)
    val personNames = attendances.map(_.person.name).removeDuplicates
    Ok(views.html.schedule(program, attendances, personNames))
  }

  def addAttendance(programId: Int) = Action(parse.urlFormEncoded) { implicit request =>
    val program = Program.findById(programId).get

    val scheduleAndChoices = program.schedules map { s =>
      (s.id.get.toInt, AttendChoice.apply(request.body.apply("attendChoice-"+s.id).head.toInt))
    } toList

    Attendance.save(
      Person(
        NotAssigned,
        request.body.apply("name").head),
      scheduleAndChoices)

    Redirect(routes.Application.showProgram(programId))
  }

  def updateAttendance(id: Int, programId: Int) = TODO

  def deleteAttendance(id: Int, programId: Int) = TODO

  def editProgram(id: Int) = Action {
    NotImplemented
  }

}