package controllers

import anorm.Id
import anorm.NotAssigned
import models.AttendChoice
import models.Attendance
import models.Person
import models.Program
import play.api.Logger
import play.api.mvc.Action
import play.api.mvc.Controller

object Attendances extends Controller with Secured {

  def addAttendance(programId: Int) = withUserUrlEncoded { user => { implicit request =>
    val program = Program.findById(programId).get

    val scheduleAndChoices = program.schedules map { s =>
      (s.id.get.toInt, AttendChoice.apply(request.body.apply("attend_choice_"+s.id).head.toInt))
    } toList

    Attendance.save(
      Person(
        NotAssigned,
        request.body.apply("new_name").head,
        Option(user.id)),
      scheduleAndChoices)

    Redirect(routes.Programs.showProgram(programId))
  }}

  def updateAttendance(programId: Int, personId: Int) = withUserUrlEncoded { user => { implicit request =>
    val program = Program.findById(programId).get
    val scheduleAndChoices = program.schedules map { s =>
      Logger.debug("attend_choice_{scheduleId}="+request.body.apply("attend_choice_"+s.id).head.toInt)
      (s.id.get.toInt, AttendChoice.apply(request.body.apply("attend_choice_"+s.id).head.toInt))
    } toList

    Attendance.update(
      Person(
        Id(personId.toLong),
        request.body.apply("name").head,
        Option(user.id)),
      scheduleAndChoices)
    Ok
  }}

  def deleteAttendance(programId: Int, personId: Int) = withAuthUrlEncoded { username => { implicit request =>
    Attendance.delete(personId)
    Ok
  }}

}