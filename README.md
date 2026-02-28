[![Twitter Follow](https://img.shields.io/twitter/follow/scala_slinq?label=follow&style=flat&logo=twitter&color=brightgreen)](https://twitter.com/scala_slinq)

# Slinq

A PostgreSQL query builder for Scala/ZIO that mirrors SQL structure directly in code.

```scala
// available for Scala 3
libraryDependencies += "io.github.karimagnusson" %% "slinq-pg-zio" % "0.9.6-RC1"
```

## Why Slinq?

Most query builders abstract SQL behind collection-like APIs. Slinq takes the opposite approach: your Scala code reads like the SQL it generates. This makes complex queries readable and the API intuitive - you already know SQL.

```scala
sql
  .select(client)
  .cols(_.all)
  .where(_.age > 25)
  .orderBy(_.username.asc)
  .limit(5)
  .run
```

## Features

- Native ZIO integration as a layer
- Full JSONB support - query, update, and return rows as JSON
- Array field operations
- Subqueries in WHERE clauses and SELECT columns
- Streaming to and from the database
- Statement caching for JDBC-level performance
- Transactions for bulk and mixed operations
- Type-safe throughout - no wildcard types with unclear errors

## Postgres by design

Slinq focuses exclusively on PostgreSQL rather than targeting lowest-common-denominator SQL. This allows deep support for Postgres-specific features like JSONB and arrays. Works with Postgres-compatible databases like CockroachDB.

## Example

```scala
import zio._
import slinq.api._

object ExampleApp extends ZIOAppDefault {

  class Client extends Model("client") {
    val id = column[Int]("id")
    val username = column[String]("username")
    val age = column[Int]("age")
    def all = (id, username, age)
  }

  val client = Model.get[Client]

  val job = for {
    _ <- sql
      .insert(client)
      .cols(t => (t.username, t.age))
      .values(("Joe", 35))
      .run

    _ <- sql
      .update(client)
      .set(_.age ==> 24)
      .where(_.id === 4)
      .run

    _ <- sql.delete(client).where(_.id === 7).run

    clients <- sql
      .select(client)
      .cols(_.all)
      .where(_.age > 25)
      .limit(5)
      .run

  } yield clients

  val dbLayer = Slinq.layer(DbConfig.forDb("company"))

  def run = job.provide(dbLayer)
}
```

## Resources

- [Full documentation](https://slinq.kotturinn.com/)

Please report bugs if you find them and feel free to DM me on Twitter if you have any questions.
