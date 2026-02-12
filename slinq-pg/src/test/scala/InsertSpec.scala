package slinq.pg

import munit.FunSuite

import api.{*, given}

class Employee extends Model("employees") {
  val id = column[Int]("id")
  val name = column[String]("name")
  val email = column[String]("email")
  val salary = column[BigDecimal]("salary")
  val departmentId = column[Int]("department_id")
}

class EmployeeArchive extends Model("employees_archive") {
  val id = column[Int]("id")
  val name = column[String]("name")
  val email = column[String]("email")
  val salary = column[BigDecimal]("salary")
  val departmentId = column[Int]("department_id")
}

class InsertSpec extends FunSuite {
  val employee = Model.get[Employee]
  val archive = Model.get[EmployeeArchive]

  test("insert with cols and values") {
    val query = sql
      .insert(employee)
      .cols(t => (t.name, t.email, t.salary, t.departmentId))
      .values(("Alice Johnson", "alice@example.com", BigDecimal(55000), 3))

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """INSERT INTO "employees" ("name", "email", "salary", "department_id") VALUES (?, ?, ?, ?)"""
    )
    assertEquals(rendered.args, Vector("Alice Johnson", "alice@example.com", BigDecimal(55000), 3))
  }

  test("insert with returning") {
    val query = sql
      .insert(employee)
      .cols(t => (t.name, t.email, t.salary, t.departmentId))
      .values(("Jane Smith", "jane@example.com", BigDecimal(60000), 2))
      .returning(_.id)

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """INSERT INTO "employees" ("name", "email", "salary", "department_id") VALUES (?, ?, ?, ?) RETURNING "id""""
    )
    assertEquals(rendered.args, Vector("Jane Smith", "jane@example.com", BigDecimal(60000), 2))
  }

  test("simple insert with data") {
    val query = sql
      .insert(employee)
      .data(t =>
        Seq(
          t.name ==> "John Doe",
          t.email ==> "john@example.com",
          t.salary ==> BigDecimal(50000),
          t.departmentId ==> 1
        )
      )

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """INSERT INTO "employees" ("name", "email", "salary", "department_id") VALUES (?, ?, ?, ?)"""
    )
    assertEquals(rendered.args, Vector("John Doe", "john@example.com", BigDecimal(50000), 1))
  }

  test("insert from select") {
    val subquery = sql
      .select(employee)
      .cols(t => (t.name, t.email, t.salary, t.departmentId))
      .where(_.salary > BigDecimal(70000))
      .asSubquery

    val query = sql
      .insert(archive)
      .cols(t => (t.name, t.email, t.salary, t.departmentId))
      .fromSelect(subquery)

    val rendered = query.render

    assertEquals(
      rendered.statement,
      """INSERT INTO "employees_archive" ("name", "email", "salary", "department_id") SELECT "a"."name", "a"."email", "a"."salary", "a"."department_id" FROM "employees" "a" WHERE "a"."salary" > ?"""
    )
    assertEquals(rendered.args, Vector(BigDecimal(70000)))
  }
}
