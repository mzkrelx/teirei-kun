package models

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB
import play.Logger
import java.sql.Connection

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
          WHERE program_id = {program_id}
          ORDER BY pe.created_at""")
        .on('program_id -> programId)()

      rows map { row =>
        Attendance(
          Person(row[Pk[Long]]("person_id"), row[String]("person_name"), row[Option[Long]]("github_user_id")),
          Schedule(row[Pk[Long]]("schedule_id"), new DateTime(row[Date]("date"))),
          AttendChoice.apply(row[Int]("choice")),
          row[Option[String]]("memo"))
      } toList
    }
  }

  def findByPersonId(personId: Int): List[Attendance] = {
    DB.withConnection { implicit c =>
      val rows = SQL("""
        SELECT *
          FROM attendance a
            INNER JOIN schedule s ON a.schedule_id = s.id
            INNER JOIN program p ON s.program_id = p.id
            INNER JOIN person pe ON a.person_id = pe.id
          WHERE person_id = {person_id}""")
        .on('person_id -> personId)()

      rows map { row =>
        Attendance(
          Person(row[Pk[Long]]("person_id"), row[String]("person_name"), row[Option[Long]]("github_user_id")),
          Schedule(row[Pk[Long]]("schedule_id"), new DateTime(row[Date]("date"))),
          AttendChoice.apply(row[Int]("choice")),
          row[Option[String]]("memo"))
      } toList
    }
  }

  def update(person: Person, attendanceRequests: List[AttendanceRequest]) {
    DB.withTransaction { implicit c =>
      updatePerson(person)

      attendanceRequests foreach { attendance =>
        val rows = SQL("""
          SELECT *
            FROM attendance
            WHERE person_id = {person_id}
            AND schedule_id = {schedule_id}""")
          .on('person_id   -> person.id,
              'schedule_id -> attendance.scheduleId)()
        rows.headOption match {
          case Some(_) => update(person.id.get, attendance)
          case None => insert(person.id.get, attendance)
        }
      }
    }
  }

  private def updatePerson(person: Person)(implicit c: Connection) {
    SQL("""
      UPDATE person SET
        person_name = {name},
        github_user_id = {github_user_id},
        updated_at = {updated_at}
      WHERE id = {id}""")
      .on('name           -> person.name,
          'github_user_id -> person.githubUserID,
          'updated_at     -> DateTime.now().toDate(),
          'id             -> person.id)
      .executeUpdate()
  }

  private def update(personId: Long, attendance: AttendanceRequest)(implicit c: Connection) {
    SQL("""
      UPDATE attendance
        SET choice = {choice},
            memo = {memo}
        WHERE person_id = {person_id}
        AND schedule_id = {schedule_id}""")
      .on('choice      -> attendance.choice.id,
          'memo        -> attendance.memo,
          'person_id   -> personId,
          'schedule_id -> attendance.scheduleId)
      .executeUpdate()
  }

  private def insert(personId: Long, attendance: AttendanceRequest)(implicit c: Connection) {
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

  def save(person: Person, attendanceRequests: List[AttendanceRequest]) {
    DB.withConnection { implicit c =>
      val personId = SQL("""
        INSERT INTO person VALUES (
          nextval('person_id_seq'),
          {name},
          {github_user_id},
          {created_at},
          {updated_at})""")
        .on('name -> person.name,
            'github_user_id -> person.githubUserID,
            'created_at     -> DateTime.now().toDate(),
            'updated_at     -> DateTime.now().toDate())
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

