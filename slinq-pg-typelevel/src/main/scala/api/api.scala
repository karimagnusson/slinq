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

package slinq.pg.typelevel

import cats.effect.IO
import fs2.{Stream, Pipe, Chunk}

import slinq.pg.select.*
import slinq.pg.update.{StoredUpdate, StoredUpdateReturning}
import slinq.pg.delete.StoredDelete
import slinq.pg.insert.StoredInsert
import slinq.pg.render.*
import slinq.pg.run.*
import slinq.pg.api.conversions
import slinq.pg.typelevel.select.Pages

package object api extends conversions {

  export slinq.pg.api.{Model, sql, Jsonb, PgConfig, SlinqError, SlinqNoRowsException, Arg, ArgSeq}

  // run query

  extension [R](rq: RunQuery[R]) {

    def run: IO[List[R]] =
      db.query(rq.render)

    def runHead: IO[R] =
      db.queryHead(rq.render)

    def runHeadOpt: IO[Option[R]] =
      db.queryHeadOpt(rq.render)

    def runAs[T](
      using transform: R => T
    ): IO[List[T]] =
      db.queryAs(rq.render, transform)

    def runHeadAs[T](
      using transform: R => T
    ): IO[T] =
      db.queryHeadAs(rq.render, transform)

    def runHeadOptAs[T](
      using transform: R => T
    ): IO[Option[T]] =
      db.queryHeadOptAs(rq.render, transform)

  }

  // run query with params

  extension [P, R](rq: RunQueryParams[P, R]) {

    def run(params: P): IO[List[R]] =
      db.query(rq.render(params))

    def runHead(params: P): IO[R] =
      db.queryHead(rq.render(params))

    def runHeadOpt(params: P): IO[Option[R]] =
      db.queryHeadOpt(rq.render(params))

    def runAs[T](params: P)(
      using transform: R => T
    ): IO[List[T]] =
      db.queryAs(rq.render(params), transform)

    def runHeadAs[T](params: P)(
      using transform: R => T
    ): IO[T] =
      db.queryHeadAs(rq.render(params), transform)

    def runHeadOptAs[T](params: P)(
      using transform: R => T
    ): IO[Option[T]] =
      db.queryHeadOptAs(rq.render(params), transform)

  }

  // run operation

  extension (ro: RunOperation) {

    def run: IO[Unit] =
      db.exec(ro.render)

    def runNum: IO[Int] =
      db.execNum(ro.render)
  }

  // transaction

  extension (t: Transaction) {

    def run: IO[Unit] =
      db.execList(t.stms)
  }

  // stream

  extension [M, R](o: Offset[M, R]) {

    def asPages(size: Int) = Pages(o.render, size)

    def stream(size: Int = 1000): Stream[IO, R] = {
      val pages = asPages(size)
      Stream.unfoldChunkEval(pages) { p =>
        p.nextOpt.map {
          case Some(rows) if rows.nonEmpty => Some((Chunk.from(rows), p))
          case _ => None
        }
      }
    }

    def streamAs[T](size: Int = 1000)(
      using transform: R => T
    ): Stream[IO, T] =
      stream(size).map(transform)
  }

  // insert

  extension [P](si: StoredInsert[P]) {

    def run(params: P): IO[Unit] =
      db.exec(si.render(params))

    def runNum(params: P): IO[Int] =
      db.execNum(si.render(params))

    def runList(paramList: Seq[P]): IO[Unit] =
      db.execList(paramList.map(si.render(_)))

    def asPipe: Pipe[IO, P, Nothing] =
      _.evalMap(params => db.exec(si.render(params))).drain

    def asListPipe: Pipe[IO, Chunk[P], Nothing] =
      _.evalMap(chunk => db.execList(chunk.toList.map(si.render(_)))).drain
  }

  // update

  extension [P1, P2](su: StoredUpdate[P1, P2]) {

    def run(p1: P1, p2: P2): IO[Unit] =
      db.exec(su.render(p1, p2))

    def runNum(p1: P1, p2: P2): IO[Int] =
      db.execNum(su.render(p1, p2))

    def runList(list: Seq[Tuple2[P1, P2]]): IO[Unit] =
      db.execList(list.map(p => su.render(p._1, p._2)))

    def asPipe: Pipe[IO, Tuple2[P1, P2], Nothing] =
      _.evalMap(p => db.exec(su.render(p._1, p._2))).drain

    def asListPipe: Pipe[IO, Chunk[Tuple2[P1, P2]], Nothing] =
      _.evalMap(chunk => db.execList(chunk.toList.map(p => su.render(p._1, p._2)))).drain
  }

  extension [P1, P2, R](sur: StoredUpdateReturning[P1, P2, R]) {

    def run(p1: P1, p2: P2): IO[List[R]] =
      db.query(sur.render(p1, p2))

    def runHead(p1: P1, p2: P2): IO[R] =
      db.queryHead(sur.render(p1, p2))

    def runHeadOpt(p1: P1, p2: P2): IO[Option[R]] =
      db.queryHeadOpt(sur.render(p1, p2))

    def runAs[T](p1: P1, p2: P2)(
      using transform: R => T
    ): IO[List[T]] =
      db.queryAs(sur.render(p1, p2), transform)

    def runHeadAs[T](p1: P1, p2: P2)(
      using transform: R => T
    ): IO[T] =
      db.queryHeadAs(sur.render(p1, p2), transform)

    def runHeadOptAs[T](p1: P1, p2: P2)(
      using transform: R => T
    ): IO[Option[T]] =
      db.queryHeadOptAs(sur.render(p1, p2), transform)
  }

  // delete

  extension [P](sd: StoredDelete[P]) {

    def run(params: P): IO[Unit] =
      db.exec(sd.render(params))

    def runNum(params: P): IO[Int] =
      db.execNum(sd.render(params))

    def runList(paramList: Seq[P]): IO[Unit] =
      db.execList(paramList.map(sd.render(_)))

    def asPipe: Pipe[IO, P, Nothing] =
      _.evalMap(params => db.exec(sd.render(params))).drain

    def asListPipe: Pipe[IO, Chunk[P], Nothing] =
      _.evalMap(chunk => db.execList(chunk.toList.map(sd.render(_)))).drain
  }

}
