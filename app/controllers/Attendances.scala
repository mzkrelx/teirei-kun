package controllers

import anorm.Id
import anorm.NotAssigned
import models.Attendance
import models.Person
import models.Program
import play.api.Logger
import play.api.mvc.Action
import play.api.mvc.Controller
import models.AttendChoice

object Attendances extends Controller {

  def addAttendance(programId: Int) = Action(parse.urlFormEncoded) { implicit request =>
    val program = Program.findById(programId).get

    val scheduleAndChoices = program.schedules map { s =>
      (s.id.get.toInt, AttendChoice.apply(request.body.apply("attend_choice_"+s.id).head.toInt))
    } toList

    Attendance.save(
      Person(
        NotAssigned,
        request.body.apply("new_name").head),
      scheduleAndChoices)

    Redirect(routes.Programs.showProgram(programId))
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

}