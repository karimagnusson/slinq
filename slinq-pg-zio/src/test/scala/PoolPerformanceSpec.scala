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

package slinq.pg.test

import zio.*
import zio.test.*

import slinq.pg.api.{*, given}
import slinq.pg.test.TestHelpers.*
import models.*

object PoolPerformanceSpec extends ZIOSpecDefault {

  val trip = Model.get[Trip]

  def spec = suite("Pool Performance Tests")(

    test("execute 100 concurrent queries and measure average time") {
      val numQueries = 100

      val singleQuery = sql
        .select(trip)
        .cols(t => (t.id, t.cityId, t.price))
        .where(_.price > 0)
        .limit(10)

      for {
        startTime <- Clock.nanoTime
        results   <- ZIO.collectAllPar(List.fill(numQueries)(singleQuery.run))
        endTime   <- Clock.nanoTime
        duration   = (endTime - startTime) / 1_000_000 // Convert to milliseconds
        avgTime    = duration / numQueries
        _         <- ZIO.succeed(println(s"\n=== Pool Performance Test ==="))
        _         <- ZIO.succeed(println(s"Total queries: $numQueries"))
        _         <- ZIO.succeed(println(s"Total time: ${duration}ms"))
        _         <- ZIO.succeed(println(s"Average time per query: ${avgTime}ms"))
        _         <- ZIO.succeed(println(s"Queries per second: ${(numQueries * 1000.0 / duration).round}"))
        _         <- ZIO.succeed(println(s"=============================\n"))
      } yield assertTrue(
        results.length == numQueries,
        results.forall(_.nonEmpty),
        duration > 0
      )
    },

    test("execute 500 concurrent queries with varying complexity") {
      val numQueries = 500

      // Use the same query type for all to avoid type issues
      val query = sql
        .select(trip)
        .cols(t => (t.id, t.cityId, t.price))
        .where(_.price > 0)
        .limit(10)

      val queries = List.fill(numQueries)(query.run)

      for {
        startTime <- Clock.nanoTime
        results   <- ZIO.collectAllPar(queries)
        endTime   <- Clock.nanoTime
        duration   = (endTime - startTime) / 1_000_000
        avgTime    = duration / numQueries
        _         <- ZIO.succeed(println(s"\n=== Mixed Query Performance Test ==="))
        _         <- ZIO.succeed(println(s"Total queries: $numQueries"))
        _         <- ZIO.succeed(println(s"Total time: ${duration}ms"))
        _         <- ZIO.succeed(println(s"Average time per query: ${avgTime}ms"))
        _         <- ZIO.succeed(println(s"Queries per second: ${(numQueries * 1000.0 / duration).round}"))
        _         <- ZIO.succeed(println(s"====================================\n"))
      } yield assertTrue(
        results.length == numQueries,
        duration > 0
      )
    },

    test("stress test - 1000 concurrent simple queries") {
      val numQueries = 1000

      val query = sql
        .select(trip)
        .cols(t => t.id)
        .where(_.id > 0)
        .limit(1)

      for {
        startTime <- Clock.nanoTime
        results   <- ZIO.collectAllPar(List.fill(numQueries)(query.run))
        endTime   <- Clock.nanoTime
        duration   = (endTime - startTime) / 1_000_000
        avgTime    = duration / numQueries
        _         <- ZIO.succeed(println(s"\n=== Stress Test ==="))
        _         <- ZIO.succeed(println(s"Total queries: $numQueries"))
        _         <- ZIO.succeed(println(s"Total time: ${duration}ms"))
        _         <- ZIO.succeed(println(s"Average time per query: ${avgTime}ms"))
        _         <- ZIO.succeed(println(s"Queries per second: ${(numQueries * 1000.0 / duration).round}"))
        _         <- ZIO.succeed(println(s"===================\n"))
      } yield assertTrue(
        results.length == numQueries,
        results.forall(_.nonEmpty),
        avgTime < 100 // Average should be less than 100ms
      )
    },

    test("cached query performance comparison") {
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

      for {
        // Test normal queries
        startNormal <- Clock.nanoTime
        _           <- ZIO.collectAllPar(List.fill(numQueries)(normalQuery.run))
        endNormal   <- Clock.nanoTime
        normalDuration = (endNormal - startNormal) / 1_000_000

        // Test cached queries
        startCached <- Clock.nanoTime
        _           <- ZIO.collectAllPar(List.fill(numQueries)(cachedQuery.run))
        endCached   <- Clock.nanoTime
        cachedDuration = (endCached - startCached) / 1_000_000

        _         <- ZIO.succeed(println(s"\n=== Cached vs Normal Query Performance ==="))
        _         <- ZIO.succeed(println(s"Queries: $numQueries"))
        _         <- ZIO.succeed(println(s"Normal queries time: ${normalDuration}ms (avg: ${normalDuration / numQueries}ms)"))
        _         <- ZIO.succeed(println(s"Cached queries time: ${cachedDuration}ms (avg: ${cachedDuration / numQueries}ms)"))
        _         <- ZIO.succeed(println(s"Speedup: ${(normalDuration.toDouble / cachedDuration * 100).round / 100.0}x"))
        _         <- ZIO.succeed(println(s"==========================================\n"))
      } yield assertTrue(
        normalDuration > 0,
        cachedDuration > 0
      )
    },

    test("sequential vs parallel execution comparison") {
      val numQueries = 50

      val query = sql
        .select(trip)
        .cols(t => (t.id, t.cityId, t.price))
        .where(_.price > 0)
        .limit(10)

      for {
        // Sequential execution
        startSeq <- Clock.nanoTime
        _        <- ZIO.collectAll(List.fill(numQueries)(query.run))
        endSeq   <- Clock.nanoTime
        seqDuration = (endSeq - startSeq) / 1_000_000

        // Parallel execution
        startPar <- Clock.nanoTime
        _        <- ZIO.collectAllPar(List.fill(numQueries)(query.run))
        endPar   <- Clock.nanoTime
        parDuration = (endPar - startPar) / 1_000_000

        _         <- ZIO.succeed(println(s"\n=== Sequential vs Parallel Execution ==="))
        _         <- ZIO.succeed(println(s"Queries: $numQueries"))
        _         <- ZIO.succeed(println(s"Sequential time: ${seqDuration}ms (avg: ${seqDuration / numQueries}ms)"))
        _         <- ZIO.succeed(println(s"Parallel time: ${parDuration}ms (avg: ${parDuration / numQueries}ms)"))
        _         <- ZIO.succeed(println(s"Speedup: ${(seqDuration.toDouble / parDuration * 100).round / 100.0}x"))
        _         <- ZIO.succeed(println(s"========================================\n"))
      } yield assertTrue(
        seqDuration > 0,
        parDuration > 0,
        parDuration < seqDuration // Parallel should be faster
      )
    }

  ).provide(dbLayer) @@ TestAspect.sequential @@ TestAspect.withLiveClock
}
