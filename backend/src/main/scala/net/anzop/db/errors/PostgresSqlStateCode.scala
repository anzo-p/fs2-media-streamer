package net.anzop.db.errors

sealed trait PostgresSqlStateCode {
  def code: String
}

case object UniqueViolation extends PostgresSqlStateCode {
  override val code = "23505"
}

case object ForeignKeyViolation extends PostgresSqlStateCode {
  override val code = "23503"
}

case object NotNullViolation extends PostgresSqlStateCode {
  override val code = "23502"
}

case object CheckViolation extends PostgresSqlStateCode {
  override val code = "23514"
}
