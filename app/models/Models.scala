package models

import java.util.Date

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB

object AttendChoice extends Enumeration {
  type AttendChoice = Value

  val Maru, Batu, Sankaku, Mitei = Value
}

object Program {

  def fromForm(title: String, description: String, schedules: String) =
    Program(NotAssigned, title, description,
      schedules.split("\r\n") .map { s =>
        Schedule(NotAssigned, DateTimeFormat.forPattern("yyyy/MM/dd").parseDateTime(s))
      } toList)

  def toForm(program: Program): Option[(String, String, String)] =
    Some(program.title, program.description,
      program.schedules map {
        _.date.toString("yyyy/MM/dd")
      } mkString "\n")

  def save(program: Program) {
    DB.withConnection { implicit c =>
      val programId = SQL("INSERT INTO program values (nextval('program_id_seq'), {title}, {description})")
        .on('title       -> program.title,
            'description -> program.description).executeInsert()
      program.schedules foreach { schedule =>
        SQL("INSERT INTO schedule values (nextval('schedule_id_seq'), {programId}, {date})")
          .on('programId -> programId,
              'date      -> schedule.date.toString("yyyy-MM-dd")).executeInsert()
      }
    }
  }

  def findById(id: Int): Option[Program] = {
    DB.withConnection { implicit c =>
      val rows = SQL("""
        SELECT * FROM program p
          INNER JOIN schedule s
            ON p.id = s.program_id
          WHERE p.id = {id}""")
        .on('id -> id)()

      rows.headOption map { head =>
        Program(
          head[Pk[Long]]("program.id"), // p.id だと実行時エラー。
          head[String]("title"),
          head[String]("description"),
          rows.map(Schedule.apply).toList)
      }
    }
  }
}

case class Program(
  id:  Pk[Long] = NotAssigned,
  title: String,
  description: String,
  schedules: List[Schedule]
)

object Schedule {

  def apply(row: SqlRow): Schedule =
    Schedule(row[Pk[Long]]("schedule.id"), new DateTime(row[Date]("date")))
}

case class Schedule(
  id: Pk[Long] = NotAssigned,
  date: DateTime
)

case class Attendance(
  person: Person,
  schedule: Schedule,
  choice: AttendChoice.Value
)

object Attendance {

  def findByProgramId(programId: Int): List[Attendance] = {
    DB.withConnection { implicit c =>
      val rows = SQL("""
        SELECT *
          FROM attendance a
            INNER JOIN schedule s ON a.schedule_id = s.id
            INNER JOIN program p ON s.program_id = p.id
            INNER JOIN person pe ON a.person_id = pe.id
          WHERE program_id = {program_id}""")
        .on('program_id -> programId)()

      rows map { row =>
        Attendance(
          Person(row[Pk[Long]]("person_id"), row[String]("person_name")),
          Schedule(row[Pk[Long]]("schedule_id"), new DateTime(row[Date]("date"))),
          AttendChoice.apply(row[Int]("choice")))
      } toList
    }
  }

  def save(attendance: Attendance) {
    DB.withConnection { implicit c =>
      SQL("""
        INSERT INTO attendance
          VALUES (
            nextval('attendance_id_seq'),
            {personId},
            {scheduleId},
            {choice})""")
        .on('personId   -> attendance.person.id,
            'scheduleId -> attendance.schedule.id,
            'choice     -> attendance.choice.id)
        .executeInsert()
    }
  }

  def save(person: Person, scheduleAndChoices: List[(Int, AttendChoice.Value)]) {
    DB.withConnection { implicit c =>
      val personId = Person.save(person).get

      scheduleAndChoices map { sc =>
        SQL("""
          INSERT INTO attendance
            VALUES (
              nextval('attendance_id_seq'),
              {personId},
              {scheduleId},
              {choice})""")
          .on('personId   -> personId,
              'scheduleId -> sc._1,
              'choice     -> sc._2.id)
          .executeInsert()
      }
    }
  }

}

case class Person(
  id: Pk[Long] = NotAssigned,
  name: String
)

object Person {

  def save(person: Person): Option[Long] = {
    DB.withConnection { implicit c =>
      SQL("INSERT INTO person values (nextval('person_id_seq'), {name})")
        .on('name -> person.name)
        .executeInsert()
    }
  }
}
