package slinq.pg

import munit.FunSuite

import api.{*, given}

class Session extends Model("sessions") {
  val id = column[Int]("id")
  val userId = column[Int]("user_id")
  val token = column[String]("token")
  val expiresAt = column[java.sql.Timestamp]("expires_at")
  val active = column[Boolean]("active")
}

class DeleteSpec extends FunSuite {
  val session = Model.get[Session]

  test("simple delete with where") {
    val query = sql
      .delete(session)
      .where(_.id === 1)

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """DELETE FROM "sessions" WHERE "id" = ?"""
    )
    assertEquals(rendered.args, Vector(1))
  }

  test("delete with multiple where conditions") {
    import slinq.pg.fn.And

    val query = sql
      .delete(session)
      .where(t => And(t.active === false, t.userId === 100))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """DELETE FROM "sessions" WHERE ("active" = ? AND "user_id" = ?)"""
    )
    assertEquals(rendered.args, Vector(false, 100))
  }

  test("delete with returning") {
    val query = sql
      .delete(session)
      .where(_.token === "abc123")
      .returning(t => (t.id, t.userId))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """DELETE FROM "sessions" WHERE "token" = ? RETURNING "id", "user_id""""
    )
    assertEquals(rendered.args, Vector("abc123"))
  }
}
