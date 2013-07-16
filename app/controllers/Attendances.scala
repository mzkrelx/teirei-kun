package controllers

import anorm.Id
import anorm.NotAssigned
import models._
import play.api.Logger
import play.api.mvc.Action
import play.api.mvc.Controller

object Attendances extends Controller with Secured {

  def showAddForm(programId: Int) = withUser { user => { implicit request =>
    val program = Program.findById(programId).get
    Ok(views.html.attendanceform(user, program))
  }}

  def addAttendance(programId: Int) = withUserUrlEncoded { user => { implicit request =>
    val program = Program.findById(programId).get

    val attendanceRequests = program.schedules map { s =>
      AttendanceRequest(s.id.get,
        AttendChoice.apply(request.body.apply("attend_choice_"+s.id).head.toInt),
        request.body.apply("memo_"+s.id).headOption)
    } toList

    Attendance.save(
      Person(
        NotAssigned,
        request.body.apply("new_name").head,
        Option(user.id)),
      attendanceRequests)

    Redirect(routes.Programs.showProgram(programId))
  }}

  def showUpdateForm(programId: Int, personId: Int) = withUser { user => { implicit request =>
    val program = Program.findById(programId).get
    val attendances = Attendance.findByPersonId(personId)
    Ok(views.html.attendanceform(user, program, Some(attendances)))
  }}

  def updateAttendance(programId: Int, personId: Int) = withUserUrlEncoded { user => { implicit request =>
    val program = Program.findById(programId).get
    val attendanceRequests = program.schedules map { s =>
      AttendanceRequest(s.id.get,
        AttendChoice.apply(request.body.apply("attend_choice_"+s.id).head.toInt),
        request.body.apply("memo_"+s.id).headOption)
    } toList

    Attendance.update(
      Person(
        Id(personId.toLong),
        request.body.apply("name").head,
        Option(user.id)),
      attendanceRequests)
    Ok
  }}

  def deleteAttendance(programId: Int, personId: Int) = withAuth { username => { implicit request =>
    Attendance.delete(personId)
    Logger.info(s"github_user_id[{$username}] deleted Attendance person_id=[{$personId}]")
    Ok
  }}

}