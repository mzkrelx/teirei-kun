package controllers

import models.Attendance
import models.Program
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.text
import play.api.mvc.Action
import play.api.mvc.Controller

object Programs extends Controller with Secured {

  val programForm: Form[Program] = Form(
    mapping(
      "title"       -> nonEmptyText,
      "description" -> text,
      "schedules"   -> nonEmptyText
    )(Program.fromForm)(Program.toForm)
  )

  def listPrograms() = withUser { user => { implicit request =>
    val programs = Program.findAll
    Ok(views.html.programs(user, programs))
  }}

  def showProgramForm = withUser { user => { implicit request =>
    Ok(views.html.programform(user, programForm))
  }}

  def addProgram = withUser { user => { implicit request =>
    programForm.bindFromRequest.fold(
      errors => BadRequest(views.html.programform(user, errors)),
      program => {
        val programId = Program.save(program)
        Redirect(routes.Programs.showProgram(programId))
      }
    )
  }}

  def showProgram(id: Int) = withUser { user => { implicit request =>
    val program = Program.findById(id)
    val attendances = Attendance.findByProgramId(id)
    Logger.debug(attendances.map(_.person).toString)
    val persons = attendances.map(_.person).distinct.toList
    Logger.debug(persons.toString)
    val ownPersonID = persons.find(p => p.githubUserID == Some(user.id)) map (_.id.get.toInt)
    Logger.debug("ownPersonID=" + ownPersonID)
    Ok(views.html.schedule(user, program, attendances, persons, ownPersonID))
  }}

  def editProgram(id: Int) = withAuth { username => { implicit request =>
    NotImplemented
  }}

}