package slinq.pg

import munit.FunSuite

import api.{*, given}

class Customer extends Model("customers") {
  val id = column[Int]("id")
  val name = column[String]("name")
  val email = column[String]("email")
  val country = column[String]("country")
}

class Purchase extends Model("purchases") {
  val id = column[Int]("id")
  val customerId = column[Int]("customer_id")
  val amount = column[BigDecimal]("amount")
  val status = column[String]("status")
}

class CountSpec extends FunSuite {
  val customer = Model.get[Customer]
  val purchase = Model.get[Purchase]

  test("count all rows in single table") {
    val query = sql
      .count(customer)
      .where(_.country === "USA")

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT count(*) FROM "customers" WHERE "country" = ?"""
    )
    assertEquals(rendered.args, Vector("USA"))
  }

  test("count with join") {
    val query = sql
      .count(customer, purchase)
      .innerJoinOn(_.id, _.customerId)
      .where(t => t.b.status === "completed")

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT count(*) FROM "customers" "a" INNER JOIN "purchases" "b" ON "a"."id" = "b"."customer_id" WHERE "b"."status" = ?"""
    )
    assertEquals(rendered.args, Vector("completed"))
  }
}
