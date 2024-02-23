package net.anzop.db.errors

import java.sql.{SQLException, SQLTimeoutException}

trait DatabaseError {
  def message: String
}

case class DuplicateEntity(message: String) extends DatabaseError
case class OtherDbError(message: String) extends DatabaseError
case class SqlError(message: String) extends DatabaseError
case class TimeoutError(message: String) extends DatabaseError
case class ValueError(message: String) extends DatabaseError

object DatabaseError {

  def handle(th: Throwable): DatabaseError =
    th match {
      case _: SQLTimeoutException => TimeoutError(th.getMessage)
      case sqlEx: SQLException =>
        sqlEx.getSQLState match {
          case UniqueViolation.code     => DuplicateEntity(th.getMessage)
          case ForeignKeyViolation.code => ValueError(th.getMessage)
          case NotNullViolation.code    => ValueError(th.getMessage)
          case CheckViolation.code      => ValueError(th.getMessage)
          case _                        => SqlError(th.getMessage)
        }
      case _ => OtherDbError(th.getMessage)
    }
}
