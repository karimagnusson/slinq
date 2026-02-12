package slinq.pg

import munit.FunSuite

import api.{*, given}

class Product extends Model("products") {
  val id = column[Int]("id")
  val name = column[String]("name")
  val price = column[BigDecimal]("price")
  val categoryId = column[Int]("category_id")
}

class Order extends Model("orders") {
  val id = column[Int]("id")
  val productId = column[Int]("product_id")
  val quantity = column[Int]("quantity")
  val total = column[BigDecimal]("total")
}

class Category extends Model("categories") {
  val id = column[Int]("id")
  val name = column[String]("name")
}

class SubquerySpec extends FunSuite {
  val product = Model.get[Product]
  val order = Model.get[Order]
  val category = Model.get[Category]

  test("simple IN subquery") {
    val subquery = sql
      .select(order)
      .cols(_.productId)
      .where(_.quantity > 10)
      .asSubquery

    val query = sql
      .select(product)
      .cols(t => (t.id, t.name))
      .where(_.id.in(subquery))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "name" FROM "products" WHERE "id" = ANY(SELECT "a"."product_id" FROM "orders" "a" WHERE "a"."quantity" > ?)"""
    )
    assertEquals(rendered.args, Vector(10))
  }

  test("NOT IN subquery") {
    val subquery = sql
      .select(order)
      .cols(_.productId)
      .where(_.total < 100)
      .asSubquery

    val query = sql
      .select(product)
      .cols(t => (t.id, t.name))
      .where(_.id.notIn(subquery))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "name" FROM "products" WHERE "id" != ANY(SELECT "a"."product_id" FROM "orders" "a" WHERE "a"."total" < ?)"""
    )
    assertEquals(rendered.args, Vector(100))
  }

  test("subquery with join") {
    val subquery = sql
      .select(order, product)
      .cols(t => t.b.categoryId)
      .innerJoinOn(_.productId, _.id)
      .where(t => t.a.quantity > 5)
      .asSubquery

    val query = sql
      .select(category)
      .cols(t => (t.id, t.name))
      .where(_.id.in(subquery))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "name" FROM "categories" WHERE "id" = ANY(SELECT "b"."category_id" FROM "orders" "a" INNER JOIN "products" "b" ON "a"."product_id" = "b"."id" WHERE "a"."quantity" > ?)"""
    )
    assertEquals(rendered.args, Vector(5))
  }

  test("nested subquery") {
    val innerSubquery = sql
      .select(order)
      .cols(_.productId)
      .where(_.total > 1000)
      .asSubquery

    val outerSubquery = sql
      .select(product)
      .cols(_.categoryId)
      .where(_.id.in(innerSubquery))
      .asSubquery

    val query = sql
      .select(category)
      .cols(t => (t.id, t.name))
      .where(_.id.in(outerSubquery))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "name" FROM "categories" WHERE "id" = ANY(SELECT "a"."category_id" FROM "products" "a" WHERE "a"."id" = ANY(SELECT "b"."product_id" FROM "orders" "b" WHERE "b"."total" > ?))"""
    )
    assertEquals(rendered.args, Vector(1000))
  }

  test("subquery with multiple conditions") {
    import slinq.pg.fn.And

    val subquery = sql
      .select(order)
      .cols(_.productId)
      .where(t => And(t.quantity > 5, t.total > 100))
      .asSubquery

    val query = sql
      .select(product)
      .cols(t => (t.id, t.name, t.price))
      .where(_.id.in(subquery))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "name", "price" FROM "products" WHERE "id" = ANY(SELECT "a"."product_id" FROM "orders" "a" WHERE ("a"."quantity" > ? AND "a"."total" > ?))"""
    )
    assertEquals(rendered.args, Vector(5, 100))
  }

  test("subquery in WHERE with additional conditions") {
    import slinq.pg.fn.And

    val subquery = sql
      .select(order)
      .cols(_.productId)
      .where(_.quantity > 10)
      .asSubquery

    val query = sql
      .select(product)
      .cols(t => (t.id, t.name))
      .where(t => And(t.id.in(subquery), t.price > 50))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "name" FROM "products" WHERE ("id" = ANY(SELECT "a"."product_id" FROM "orders" "a" WHERE "a"."quantity" > ?) AND "price" > ?)"""
    )
    assertEquals(rendered.args, Vector(10, 50))
  }
}
