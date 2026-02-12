package slinq.pg

import munit.FunSuite

import api.{*, given}

class Account extends Model("accounts") {
  val id = column[Int]("id")
  val username = column[String]("username")
  val email = column[String]("email")
  val balance = column[BigDecimal]("balance")
  val status = column[String]("status")
}

class UpdateSpec extends FunSuite {
  val account = Model.get[Account]

  test("simple update with where") {
    val query = sql
      .update(account)
      .set(t => Seq(
        t.status ==> "active",
        t.balance ==> BigDecimal(1000)
      ))
      .where(_.id === 1)

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """UPDATE "accounts" SET "status" = ?, "balance" = ? WHERE "id" = ?"""
    )
    assertEquals(rendered.args, Vector("active", BigDecimal(1000), 1))
  }

  test("update with multiple where conditions") {
    import slinq.pg.fn.And

    val query = sql
      .update(account)
      .set(t => Seq(t.status ==> "inactive"))
      .where(t => And(t.balance < BigDecimal(10), t.status === "active"))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """UPDATE "accounts" SET "status" = ? WHERE ("balance" < ? AND "status" = ?)"""
    )
    assertEquals(rendered.args, Vector("inactive", BigDecimal(10), "active"))
  }

  test("update with returning") {
    val query = sql
      .update(account)
      .set(t => Seq(
        t.balance ==> BigDecimal(500),
        t.status ==> "updated"
      ))
      .where(_.id === 2)
      .returning(t => (t.id, t.balance))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """UPDATE "accounts" SET "balance" = ?, "status" = ? WHERE "id" = ? RETURNING "id", "balance""""
    )
    assertEquals(rendered.args, Vector(BigDecimal(500), "updated", 2))
  }
}
