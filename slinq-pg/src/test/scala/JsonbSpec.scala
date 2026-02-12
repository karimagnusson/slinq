package slinq.pg

import munit.FunSuite

import api.{*, given}

class Document extends Model("documents") {
  val id = column[Int]("id")
  val title = column[String]("title")
  val metadata = column[Jsonb]("metadata")
  val tags = column[Seq[String]]("tags")
}

class JsonbSpec extends FunSuite {
  val doc = Model.get[Document]

  test("query jsonb field with -> operator") {
    val query = sql
      .select(doc)
      .cols(t => (t.id, t.title, t.metadata -> "author"))
      .where(_.id === 1)

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "title", "metadata"->? FROM "documents" WHERE "id" = ?"""
    )
    assertEquals(rendered.args, Vector("author", 1))
  }

  test("query jsonb field with ->> operator for text") {
    val query = sql
      .select(doc)
      .cols(t => (t.id, t.metadata ->> "category"))
      .where(t => (t.metadata ->> "status") === "published")

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "metadata"->>? FROM "documents" WHERE "metadata"->>? = ?"""
    )
    assertEquals(rendered.args, Vector("category", "status", "published"))
  }

  test("query jsonb with path operator #>>") {
    val query = sql
      .select(doc)
      .cols(t => (t.id, t.metadata #>> Seq("user", "name")))
      .where(_.id > 0)

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "metadata"#>>? FROM "documents" WHERE "id" > ?"""
    )
    // The args contain a TypeArray wrapper for the sequence
    assert(rendered.args.size == 2)
    assertEquals(rendered.args(1), 0)
  }

  test("query jsonb with contains operator @>") {
    val query = sql
      .select(doc)
      .cols(t => (t.id, t.title))
      .where(_.metadata @> Jsonb("""{"featured": true}"""))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "title" FROM "documents" WHERE "metadata" @> ?"""
    )
    assertEquals(rendered.args, Vector(Jsonb("""{"featured": true}""")))
  }

  test("query jsonb with exists operator ?") {
    val query = sql
      .select(doc)
      .cols(t => (t.id, t.title))
      .where(_.metadata ? "author")

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """SELECT "id", "title" FROM "documents" WHERE "metadata" ?? ?"""
    )
    assertEquals(rendered.args, Vector("author"))
  }

  test("update jsonb field") {
    val query = sql
      .update(doc)
      .set(t => Seq(
        t.metadata ==> Jsonb("""{"updated": true}"""),
        t.title ==> "Updated Title"
      ))
      .where(_.id === 1)

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """UPDATE "documents" SET "metadata" = ?, "title" = ? WHERE "id" = ?"""
    )
    assertEquals(rendered.args, Vector(Jsonb("""{"updated": true}"""), "Updated Title", 1))
  }
}
