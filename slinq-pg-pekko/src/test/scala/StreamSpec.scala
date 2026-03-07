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

package slinq.pg.pekko.test

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.*

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Source, Sink}

import slinq.pg.pekko.api.{*, given}
import slinq.pg.pekko.test.TestHelpers.*
import models.*

class StreamSpec extends munit.FunSuite {

  val trip = Model.get[Trip]

  implicit val system: ActorSystem = ActorSystem("stream-test")

  override def beforeAll(): Unit = {
    initDb()
  }

  override def afterAll(): Unit = {
    system.terminate()
  }

  test("source should stream all trip rows") {
    val query = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.price > 0)
      .orderBy(_.id.asc)

    val resultsFuture = query.source(10).runWith(Sink.seq)

    val results = Await.result(resultsFuture, 30.seconds)
    assert(results.nonEmpty)
  }

  test("source with small page size should return same results as large page size") {
    val query = sql
      .select(trip)
      .cols(t => (t.id, t.price))
      .where(_.price > 0)
      .orderBy(_.id.asc)

    val smallPages = Await.result(query.source(5).runWith(Sink.seq), 30.seconds)
    val largePages = Await.result(query.source(100).runWith(Sink.seq), 30.seconds)

    assertEquals(smallPages.length, largePages.length)
    assertEquals(smallPages.toList, largePages.toList)
  }

  test("insert sink should write rows via stream") {
    val testCityId = 777

    val storedInsert = sql
      .insert(trip)
      .cols(t => (t.cityId, t.price))
      .cache

    val insertData = Seq((testCityId, 100), (testCityId, 200), (testCityId, 300))

    val insertFuture = Source(insertData).runWith(storedInsert.asSink)
    Await.result(insertFuture, 30.seconds)

    val selectQuery = sql
      .select(trip)
      .cols(t => (t.cityId, t.price))
      .where(_.cityId === testCityId)

    val results = Await.result(selectQuery.run, 30.seconds)
    assertEquals(results.length, 3)

    val deleteQuery = sql
      .delete(trip)
      .where(_.cityId === testCityId)

    Await.result(deleteQuery.run, 30.seconds)
  }

  test("asPages should paginate results") {
    val query = sql
      .select(trip)
      .cols(t => (t.id, t.price))
      .where(_.price > 0)
      .orderBy(_.id.asc)

    val pages = query.asPages(5)

    val page1 = Await.result(pages.next, 30.seconds)
    assert(page1.nonEmpty)
    assert(page1.length <= 5)

    val page2 = Await.result(pages.next, 30.seconds)
    if (page2.nonEmpty) {
      assert(page2.length <= 5)
      assert(page1.head != page2.head)
    }
  }

  test("specific page number should return correct results") {
    val query = sql
      .select(trip)
      .cols(t => (t.id, t.price))
      .where(_.price > 0)
      .orderBy(_.id.asc)

    val pages = query.asPages(5)

    val page1 = Await.result(pages.page(1), 30.seconds)
    val page2 = Await.result(pages.page(2), 30.seconds)

    assert(page1.nonEmpty)
    if (page2.nonEmpty) {
      assert(page1.head != page2.head)
    }
  }
}
