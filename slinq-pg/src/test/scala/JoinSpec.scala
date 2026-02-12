package slinq.pg

import munit.FunSuite

import api.{*, given}

class Author extends Model("users") {
  val id = column[Int]("id")
  val name = column[String]("name")
  val email = column[String]("email")
}

class Article extends Model("posts") {
  val id = column[Int]("id")
  val userId = column[Int]("user_id")
  val title = column[String]("title")
  val content = column[String]("content")
}

class JoinSpec extends FunSuite {
  val author = Model.get[Author]
  val article = Model.get[Article]

  test("simple inner join") {
    val query = sql
      .select(author, article)
      .cols(t => (t.a.id, t.a.name, t.b.title))
      .innerJoinOn(_.id, _.userId)
      .all

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "a"."id", "a"."name", "b"."title" FROM "users" "a" INNER JOIN "posts" "b" ON "a"."id" = "b"."user_id""""
    )
  }

  test("join with where clause") {
    val query = sql
      .select(author, article)
      .cols(t => (t.a.name, t.b.title))
      .innerJoinOn(_.id, _.userId)
      .where(t => t.a.id === 1)

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "a"."name", "b"."title" FROM "users" "a" INNER JOIN "posts" "b" ON "a"."id" = "b"."user_id" WHERE "a"."id" = ?"""
    )
    assertEquals(rendered.args, Vector(1))
  }

  test("join with multiple conditions") {
    import slinq.pg.fn.And

    val query = sql
      .select(author, article)
      .cols(t => (t.a.name, t.b.title))
      .innerJoinOn(_.id, _.userId)
      .where(t => And(t.a.id > 10, t.b.title.like("Scala%")))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "a"."name", "b"."title" FROM "users" "a" INNER JOIN "posts" "b" ON "a"."id" = "b"."user_id" WHERE ("a"."id" > ? AND "b"."title" LIKE concat('%', ?, '%'))"""
    )
    assertEquals(rendered.args, Vector(10, "Scala%"))
  }

  test("left join") {
    val query = sql
      .select(author, article)
      .cols(t => (t.a.id, t.a.name, t.b.title))
      .leftJoinOn(_.id, _.userId)
      .all

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "a"."id", "a"."name", "b"."title" FROM "users" "a" LEFT JOIN "posts" "b" ON "a"."id" = "b"."user_id""""
    )
  }

  test("right join") {
    val query = sql
      .select(author, article)
      .cols(t => (t.a.name, t.b.title))
      .rightJoinOn(_.id, _.userId)
      .all

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "a"."name", "b"."title" FROM "users" "a" RIGHT JOIN "posts" "b" ON "a"."id" = "b"."user_id""""
    )
  }

  test("full outer join") {
    val query = sql
      .select(author, article)
      .cols(t => (t.a.name, t.b.title))
      .fullOuterJoinOn(_.id, _.userId)
      .all

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "a"."name", "b"."title" FROM "users" "a" FULL OUTER JOIN "posts" "b" ON "a"."id" = "b"."user_id""""
    )
  }

  test("cross join") {
    val query = sql
      .select(author, article)
      .cols(t => (t.a.name, t.b.title))
      .crossJoin
      .all

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "a"."name", "b"."title" FROM "users" "a" CROSS JOIN "posts" "b""""
    )
  }
}
