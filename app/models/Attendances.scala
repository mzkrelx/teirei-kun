package models

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB
import play.Logger

object AttendChoice extends Enumeration {
  type AttendChoice = Value

  val Maru, Batu, Sankaku, Mitei = Value
}

case class Attendance(
  person: Person,
  schedule: Schedule,
  choice: AttendChoice.Value,
  memo: Option[String]
)

case class AttendanceRequest(
  scheduleId: Long,
  choice: AttendChoice.Value,
  memo: Option[String]
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
          Person(row[Pk[Long]]("person_id"), row[String]("person_name"), row[Option[Long]]("github_user_id")),
          Schedule(row[Pk[Long]]("schedule_id"), new DateTime(row[Date]("date"))),
          AttendChoice.apply(row[Int]("choice")),
          row[String]("memo") match {
            case "" => None
            case s  => Some(s)
          })
      } toList
    }
  }

  def update(person: Person, scheduleAndChoices: List[(Int, AttendChoice.Value)]) {
    DB.withTransaction { implicit c =>
      SQL("""
        UPDATE person SET
          person_name = {name},
          github_user_id = {github_user_id}
        WHERE id = {id}""")
        .on('name           -> person.name,
            'github_user_id -> person.githubUserID,
            'id             -> person.id)
        .executeUpdate()

      scheduleAndChoices foreach { x =>
        val rows = SQL("""
          SELECT *
            FROM attendance
            WHERE person_id = {person_id}
            AND schedule_id = {schedule_id}""")
          .on('person_id   -> person.id,
              'schedule_id -> x._1)()
        rows.headOption match {
          case Some(_) => SQL("""
            UPDATE attendance SET choice = {choice}
              WHERE person_id = {person_id}
              AND schedule_id = {schedule_id}""")
            .on('choice      -> x._2.id,
                'person_id   -> person.id,
                'schedule_id -> x._1)
            .executeUpdate()
          case None => SQL("""
            INSERT INTO attendance
              VALUES (
                nextval('attendance_id_seq'),
                {person_id},
                {schedule_id},
                {choice})""")
            .on('person_id   -> person.id,
                'schedule_id -> x._1,
                'choice     -> x._2.id)
            .executeInsert()
        }
      }
    }
  }

  def save(person: Person, attendanceRequests: List[AttendanceRequest]) {
    DB.withConnection { implicit c =>
      val personId = SQL("""
        INSERT INTO person VALUES (
          nextval('person_id_seq'),
          {name},
          {github_user_id})""")
        .on('name -> person.name,
            'github_user_id -> person.githubUserID)
        .executeInsert().get

      attendanceRequests map { attendance =>
        SQL("""
          INSERT INTO attendance
            VALUES (
              nextval('attendance_id_seq'),
              {person_id},
              {schedule_id},
              {choice},
              {memo})""")
          .on('person_id   -> personId,
              'schedule_id -> attendance.scheduleId,
              'choice      -> attendance.choice.id,
              'memo        -> attendance.memo)
          .executeInsert()
      }
    }
  }

  def delete(personId: Int) {
    DB.withTransaction { implicit c =>
      SQL("""
        DELETE
          FROM attendance
          WHERE person_id = {person_id}""")
        .on('person_id -> personId)
        .executeUpdate()

      SQL("""
        DELETE FROM person
          WHERE id = {id}""")
        .on('id   -> personId)
        .executeUpdate()
    }
  }

}

case class Person(
  id: Pk[Long] = NotAssigned,
  name: String,
  githubUserID: Option[Long]
)

