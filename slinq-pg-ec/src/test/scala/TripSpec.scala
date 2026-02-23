/*
 * Copyright 2021 Kári Magnússon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package slinq.pg.ec.test

import slinq.pg.ec.api.{*, given}
import slinq.pg.ec.test.TestHelpers.*
import models.*

class TripSpec extends munit.FunSuite {

  val trip = Model.get[Trip]

  override def beforeAll(): Unit = {
    initDb()
  }

  // Query Rendering Tests

  test("select all trips should render correctly") {
    val query = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.id > 0)

    val rendered = query.render

    assertEquals(rendered.statement, """SELECT "id", "city_id", "price" FROM "trip" WHERE "id" > ?""")
    assertEquals(rendered.args, Vector(0))
  }

  test("select with where clause should render correctly") {
    val query = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.price > 100)

    val rendered = query.render

    assertEquals(rendered.statement, """SELECT "id", "city_id", "price" FROM "trip" WHERE "price" > ?""")
    assertEquals(rendered.args, Vector(100))
  }

  test("insert trip should render correctly") {
    val query = sql
      .insert(trip)
      .cols(t => (t.cityId, t.price))
      .values((1, 150))

    val rendered = query.render

    assertEquals(rendered.statement, """INSERT INTO "trip" ("city_id", "price") VALUES (?, ?)""")
    assertEquals(rendered.args, Vector(1, 150))
  }

  test("update trip should render correctly") {
    val query = sql
      .update(trip)
      .set(t => Seq(t.price ==> 200))
      .where(_.id === 1)

    val rendered = query.render

    assertEquals(rendered.statement, """UPDATE "trip" SET "price" = ? WHERE "id" = ?""")
    assertEquals(rendered.args, Vector(200, 1))
  }

  test("delete trip should render correctly") {
    val query = sql
      .delete(trip)
      .where(_.id === 1)

    val rendered = query.render

    assertEquals(rendered.statement, """DELETE FROM "trip" WHERE "id" = ?""")
    assertEquals(rendered.args, Vector(1))
  }

  // Database Integration Tests

  test("insert and query trip should work correctly") {
    val testCityId = 999
    val testPrice = 250

    val insertQuery = sql
      .insert(trip)
      .cols(t => (t.cityId, t.price))
      .values((testCityId, testPrice))

    val selectQuery = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.cityId === testCityId)

    val deleteQuery = sql
      .delete(trip)
      .where(_.cityId === testCityId)

    for {
      _       <- insertQuery.run
      results <- selectQuery.run
      _       <- deleteQuery.run
    } yield {
      assert(results.nonEmpty)
      assert(results.exists { case (_, cityId, price) => cityId == testCityId && price == testPrice })
    }
  }

  test("select trips with limit should return limited results") {
    val query = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.id > 0)
      .limit(5)

    for {
      results <- query.run
    } yield {
      assert(results.length <= 5)
    }
  }

  test("select trips with ordering should return ordered results") {
    val query = sql
      .select(trip)
      .cols(t => (t.id, t.price))
      .where(_.price > 0)
      .orderBy(_.price.desc)
      .limit(10)

    for {
      results <- query.run
    } yield {
      assert(results.isInstanceOf[List[(Long, Int)]])
      assert(results.length <= 10)
    }
  }

  test("cached query execution should return consistent results") {
    val cachedQuery = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.price > 0)
      .limit(3)
      .cache

    for {
      results1 <- cachedQuery.run
      results2 <- cachedQuery.run
    } yield {
      assertEquals(results1, results2)
      assert(results1.length <= 3)
    }
  }

  test("update and verify trip price should update correctly") {
    val testCityId = 888
    val initialPrice = 100
    val updatedPrice = 300

    val insertQuery = sql
      .insert(trip)
      .cols(t => (t.cityId, t.price))
      .values((testCityId, initialPrice))

    val updateQuery = sql
      .update(trip)
      .set(t => Seq(t.price ==> updatedPrice))
      .where(_.cityId === testCityId)
      .cache

    val selectQuery = sql
      .select(trip)
      .cols(t => t.price)
      .where(_.cityId === testCityId)

    val deleteQuery = sql
      .delete(trip)
      .where(_.cityId === testCityId)

    for {
      _            <- insertQuery.run
      _            <- updateQuery.run
      priceResults <- selectQuery.run
      _            <- deleteQuery.run
    } yield {
      assertEquals(priceResults.headOption, Some(updatedPrice))
    }
  }
}
