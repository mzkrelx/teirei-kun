package controllers

import anorm.NotAssigned
import anorm.Pk
import models.AttendChoice
import models.Attendance
import models.Person
import models.Program
import models.Schedule
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import anorm.Id

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

  def listPrograms() = Action {
    val programs = Program.findAll
    Ok(views.html.programs(programs))
  }

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
    Logger.debug(attendances.map(_.person).toString)
    val persons = attendances.map(_.person).removeDuplicates.toList
    Logger.debug(persons.toString)
    Ok(views.html.schedule(program, attendances, persons))
  }

  def addAttendance(programId: Int) = Action(parse.urlFormEncoded) { implicit request =>
    val program = Program.findById(programId).get

    val scheduleAndChoices = program.schedules map { s =>
      (s.id.get.toInt, AttendChoice.apply(request.body.apply("attend_choice_"+s.id).head.toInt))
    } toList

    Attendance.save(
      Person(
        NotAssigned,
        request.body.apply("name").head),
      scheduleAndChoices)

    Redirect(routes.Application.showProgram(programId))
  }

  def updateAttendance(programId: Int, personId: Int) = Action(parse.urlFormEncoded) { implicit request =>
    val program = Program.findById(programId).get
    val scheduleAndChoices = program.schedules map { s =>
      Logger.debug("attend_choice_{scheduleId}="+request.body.apply("attend_choice_"+s.id).head.toInt)
      (s.id.get.toInt, AttendChoice.apply(request.body.apply("attend_choice_"+s.id).head.toInt))
    } toList

    Attendance.update(
      Person(
        Id(personId.toLong),
        request.body.apply("name").head),
      scheduleAndChoices)
    Ok
  }

  def deleteAttendance(programId: Int, personId: Int) = Action { implicit request =>
    Attendance.delete(personId)
    Ok
  }

  def editProgram(id: Int) = Action {
    NotImplemented
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRouter", Some("jQuery.ajax"))(
      routes.javascript.Application.updateAttendance,
      routes.javascript.Application.deleteAttendance)
    ).as("text/javascript")
  }

}