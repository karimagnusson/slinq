package slinq.pg

import munit.FunSuite

import api.{*, given}

class User extends Model("users") {
  val id = column[Int]("id")
  val name = column[String]("name")
  val email = column[String]("email")
  val age = column[Int]("age")
}

class SelectSpec extends FunSuite {
  val user = new User

  test("simple select all columns") {
    val query = sql
      .select(user)
      .cols(t => (t.id, t.name, t.email, t.age))
      .where(_.id === 1)

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "name", "email", "age" FROM "users" WHERE "id" = ?"""
    )
    assertEquals(rendered.args, Vector(1))
  }

  test("select specific columns") {
    val query = sql
      .select(user)
      .cols(t => (t.name, t.email))
      .where(_.age > 18)

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "name", "email" FROM "users" WHERE "age" > ?"""
    )
    assertEquals(rendered.args, Vector(18))
  }

  test("select with and filter") {
    import slinq.pg.fn.And

    val query = sql
      .select(user)
      .cols(t => (t.id, t.name))
      .where(t => And(t.age > 18, t.name === "John"))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "name" FROM "users" WHERE ("age" > ? AND "name" = ?)"""
    )
    assertEquals(rendered.args, Vector(18, "John"))
  }

  test("cached select") {
    val cachedQuery = sql
      .select(user)
      .cols(t => (t.id, t.name))
      .where(_.age > 25)
      .cache

    val rendered = cachedQuery.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "name" FROM "users" WHERE "age" > ?"""
    )
    assertEquals(rendered.args, Vector(25))
  }

}
