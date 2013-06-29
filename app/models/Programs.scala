package models

import java.util.Date

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import anorm._
import play.api.db.DB
import play.api.Play.current

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

  def save(program: Program): Int = {
    DB.withConnection { implicit c =>
      val programId = SQL("INSERT INTO program values (nextval('program_id_seq'), {title}, {description})")
        .on('title       -> program.title,
            'description -> program.description).executeInsert()
      program.schedules foreach { schedule =>
        SQL("INSERT INTO schedule values (nextval('schedule_id_seq'), {program_id}, {date})")
          .on('program_id -> programId,
              'date       -> schedule.date.toDate).executeInsert()
      }
      programId.get.toInt
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

  def findAll(): List[Program] =
    DB.withConnection {
      implicit c => {
        val schedules = SQL("SELECT * FROM schedule")() map { row =>
          row[Long]("program_id") -> Schedule(row[Pk[Long]]("id"), new DateTime(row[Date]("date")))
        } toList

        SQL("SELECT * FROM program")() map { row =>
          val programId = row[Pk[Long]]("id")
          Program(programId, row[String]("title"), row[String]("description"),
            schedules.withFilter(_._1 == programId.get).map(_._2))
        } toList
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

