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

import slinq.pg.pekko.api.{*, given}
import slinq.pg.pekko.test.TestHelpers.*
import models.*

class PoolPerformanceSpec extends munit.FunSuite {

  val trip = Model.get[Trip]

  override def beforeAll(): Unit = {
    initDb()
  }

  test("100 concurrent queries should execute and measure average time") {
    val numQueries = 100

    val singleQuery = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.price > 0)
      .limit(10)

    val startTime = System.nanoTime()

    val resultsFuture = Future.sequence(List.fill(numQueries)(singleQuery.run)).map { results =>
      val endTime = System.nanoTime()
      val duration = (endTime - startTime) / 1_000_000
      val avgTime = duration / numQueries

      println(s"\n=== Pool Performance Test ===")
      println(s"Total queries: $numQueries")
      println(s"Total time: ${duration}ms")
      println(s"Average time per query: ${avgTime}ms")
      println(s"Queries per second: ${(numQueries * 1000.0 / duration).round}")
      println(s"=============================\n")

      assertEquals(results.length, numQueries)
      assert(results.forall(_.nonEmpty))
      assert(duration > 0L)
    }

    Await.result(resultsFuture, 60.seconds)
  }

  test("500 concurrent queries should execute with varying complexity") {
    val numQueries = 500

    val query = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.price > 0)
      .limit(10)

    val queries = List.fill(numQueries)(query.run)

    val startTime = System.nanoTime()

    val resultsFuture = Future.sequence(queries).map { results =>
      val endTime = System.nanoTime()
      val duration = (endTime - startTime) / 1_000_000
      val avgTime = duration / numQueries

      println(s"\n=== Mixed Query Performance Test ===")
      println(s"Total queries: $numQueries")
      println(s"Total time: ${duration}ms")
      println(s"Average time per query: ${avgTime}ms")
      println(s"Queries per second: ${(numQueries * 1000.0 / duration).round}")
      println(s"====================================\n")

      assertEquals(results.length, numQueries)
      assert(duration > 0L)
    }

    Await.result(resultsFuture, 120.seconds)
  }

  test("1000 concurrent simple queries should pass stress test") {
    val numQueries = 1000

    val query = sql
      .select(trip)
      .cols(t => t.id)
      .where(_.id > 0)
      .limit(1)

    val startTime = System.nanoTime()

    val resultsFuture = Future.sequence(List.fill(numQueries)(query.run)).map { results =>
      val endTime = System.nanoTime()
      val duration = (endTime - startTime) / 1_000_000
      val avgTime = duration / numQueries

      println(s"\n=== Stress Test ===")
      println(s"Total queries: $numQueries")
      println(s"Total time: ${duration}ms")
      println(s"Average time per query: ${avgTime}ms")
      println(s"Queries per second: ${(numQueries * 1000.0 / duration).round}")
      println(s"===================\n")

      assertEquals(results.length, numQueries)
      assert(results.forall(_.nonEmpty))
      assert(avgTime < 100L)
    }

    Await.result(resultsFuture, 180.seconds)
  }

  test("cached query should perform better than normal queries") {
    val numQueries = 200

    val normalQuery = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.price > 0)
      .limit(10)

    val cachedQuery = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.price > 0)
      .limit(10)
      .cache

    val startNormal = System.nanoTime()

    val resultsFuture = Future.sequence(List.fill(numQueries)(normalQuery.run)).flatMap { _ =>
      val endNormal = System.nanoTime()
      val normalDuration = (endNormal - startNormal) / 1_000_000

      val startCached = System.nanoTime()

      Future.sequence(List.fill(numQueries)(cachedQuery.run)).map { _ =>
        val endCached = System.nanoTime()
        val cachedDuration = (endCached - startCached) / 1_000_000

        println(s"\n=== Cached vs Normal Query Performance ===")
        println(s"Queries: $numQueries")
        println(s"Normal queries time: ${normalDuration}ms (avg: ${normalDuration / numQueries}ms)")
        println(s"Cached queries time: ${cachedDuration}ms (avg: ${cachedDuration / numQueries}ms)")
        println(s"Speedup: ${(normalDuration.toDouble / cachedDuration * 100).round / 100.0}x")
        println(s"==========================================\n")

        assert(normalDuration > 0L)
        assert(cachedDuration > 0L)
      }
    }

    Await.result(resultsFuture, 120.seconds)
  }

  test("sequential vs parallel execution should show parallel is faster") {
    val numQueries = 50

    val query = sql
      .select(trip)
      .cols(t => (t.id, t.cityId, t.price))
      .where(_.price > 0)
      .limit(10)

    def runSequential(n: Int): Future[List[List[(Long, Int, Int)]]] =
      if (n <= 0) Future.successful(Nil)
      else query.run.flatMap(r => runSequential(n - 1).map(r :: _))

    val startSeq = System.nanoTime()

    val resultsFuture = runSequential(numQueries).flatMap { _ =>
      val endSeq = System.nanoTime()
      val seqDuration = (endSeq - startSeq) / 1_000_000

      val startPar = System.nanoTime()

      Future.sequence(List.fill(numQueries)(query.run)).map { _ =>
        val endPar = System.nanoTime()
        val parDuration = (endPar - startPar) / 1_000_000

        println(s"\n=== Sequential vs Parallel Execution ===")
        println(s"Queries: $numQueries")
        println(s"Sequential time: ${seqDuration}ms (avg: ${seqDuration / numQueries}ms)")
        println(s"Parallel time: ${parDuration}ms (avg: ${parDuration / numQueries}ms)")
        println(s"Speedup: ${(seqDuration.toDouble / parDuration * 100).round / 100.0}x")
        println(s"========================================\n")

        assert(seqDuration > 0L)
        assert(parDuration > 0L)
        assert(parDuration < seqDuration)
      }
    }

    Await.result(resultsFuture, 120.seconds)
  }
}
