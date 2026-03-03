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

package slinq.pg.zio

import java.sql.SQLException

import slinq.pg.select.*
import slinq.pg.update.{StoredUpdate, StoredUpdateReturning}
import slinq.pg.delete.StoredDelete
import slinq.pg.insert.{StoredInsert, StoredInsertReturningTyped}
import slinq.pg.render.*
import slinq.pg.run.*
import slinq.pg.api.conversions
import slinq.pg.zio.select.{Pages, StreamQuery}

import _root_.zio.*
import _root_.zio.stream.{ZStream, ZSink}

package object api extends conversions {

  export slinq.pg.api.{Model, sql, Jsonb, PgConfig, SlinqError, SlinqNoRowsException, Arg, ArgSeq}

  // run query

  extension [R](rq: RunQuery[R]) {

    def run: ZIO[SlinqPg, SQLException, List[R]] =
      db.query(rq.render)

    def runHead: ZIO[SlinqPg, SQLException, R] =
      db.queryHead(rq.render)

    def runHeadOpt: ZIO[SlinqPg, SQLException, Option[R]] =
      db.queryHeadOpt(rq.render)

    def runAs[T](
      using transform: R => T
    ): ZIO[SlinqPg, SQLException, List[T]] =
      db.queryAs(rq.render, transform)

    def runHeadAs[T](
      using transform: R => T
    ): ZIO[SlinqPg, SQLException, T] =
      db.queryHeadAs(rq.render, transform)

    def runHeadOptAs[T](
      using transform: R => T
    ): ZIO[SlinqPg, SQLException, Option[T]] =
      db.queryHeadOptAs(rq.render, transform)
  }

  // run query with params

  extension [P, R](rq: RunQueryParams[P, R]) {

    def run(params: P): ZIO[SlinqPg, SQLException, List[R]] =
      db.query(rq.render(params))

    def runHead(params: P): ZIO[SlinqPg, SQLException, R] =
      db.queryHead(rq.render(params))

    def runHeadOpt(params: P): ZIO[SlinqPg, SQLException, Option[R]] =
      db.queryHeadOpt(rq.render(params))

    def runAs[T](params: P)(
      using transform: R => T
    ): ZIO[SlinqPg, SQLException, List[T]] =
      db.queryAs(rq.render(params), transform)

    def runHeadAs[T](params: P)(
      using transform: R => T
    ): ZIO[SlinqPg, SQLException, T] =
      db.queryHeadAs(rq.render(params), transform)

    def runHeadOptAs[T](params: P)(
      using transform: R => T
    ): ZIO[SlinqPg, SQLException, Option[T]] =
      db.queryHeadOptAs(rq.render(params), transform)
  }

  // run operation

  extension (ro: RunOperation) {

    def run = db.exec(ro.render)

    def runNum = db.execNum(ro.render)
  }

  // transaction

  extension (t: Transaction) {

    def run = db.execList(t.stms)
  }

  // stream

  extension [M, R](o: Offset[M, R]) {

    def asPages(size: Int) = Pages(o.render, size)

    def stream: ZStream[SlinqPg, SQLException, R] = stream(1000)

    def stream(size: Int): ZStream[SlinqPg, SQLException, R] = {
      val gen = new StreamQuery(asPages(size))
      ZStream.unfoldChunkZIO(gen)(a => a.next)
    }

    def streamAs[T](
      using transform: R => T
    ): ZStream[SlinqPg, SQLException, T] =
      stream.map(transform)

    def streamAs[T](size: Int)(
      using transform: R => T
    ): ZStream[SlinqPg, SQLException, T] =
      stream(size).map(transform)
  }

  // insert

  extension [P](si: StoredInsert[P]) {

    def run(params: P): ZIO[SlinqPg, SQLException, Unit] =
      db.exec(si.render(params))

    def runNum(params: P): ZIO[SlinqPg, SQLException, Int] =
      db.execNum(si.render(params))

    def runList(paramList: Seq[P]): ZIO[SlinqPg, SQLException, Unit] =
      db.execList(paramList.map(si.render(_)))

    def collect(size: Int): ZSink[Any, Nothing, P, P, Chunk[P]] =
      ZSink.collectAllN[P](size)

    def asSink: ZSink[SlinqPg, SQLException, P, Nothing, Unit] =
      ZSink.foreach((params: P) => db.exec(si.render(params)))

    def asListSink: ZSink[SlinqPg, SQLException, Chunk[P], Nothing, Unit] =
      ZSink.foreach { (chunk: Chunk[P]) =>
        db.execList(chunk.toList.map(p => si.render(p)))
      }
  }

  // update

  extension [P1, P2](su: StoredUpdate[P1, P2]) {

    def run(p1: P1, p2: P2): ZIO[SlinqPg, SQLException, Unit] =
      db.exec(su.render(p1, p2))

    def runNum(p1: P1, p2: P2): ZIO[SlinqPg, SQLException, Int] =
      db.execNum(su.render(p1, p2))

    def runList(list: Seq[Tuple2[P1, P2]]): ZIO[SlinqPg, SQLException, Unit] =
      db.execList(list.map(p => su.render(p._1, p._2)))

    def collect(
      size: Int
    ): ZSink[Any, Nothing, Tuple2[P1, P2], Tuple2[P1, P2], Chunk[Tuple2[P1, P2]]] =
      ZSink.collectAllN[Tuple2[P1, P2]](size)

    def asSink: ZSink[SlinqPg, SQLException, Tuple2[P1, P2], Nothing, Unit] =
      ZSink.foreach { (p: Tuple2[P1, P2]) =>
        db.exec(su.render(p._1, p._2))
      }

    def asListSink: ZSink[SlinqPg, SQLException, Chunk[Tuple2[P1, P2]], Nothing, Unit] =
      ZSink.foreach { (chunk: Chunk[Tuple2[P1, P2]]) =>
        db.execList(chunk.toList.map(p => su.render(p._1, p._2)))
      }
  }

  extension [P1, P2, R](sur: StoredUpdateReturning[P1, P2, R]) {

    def run(p1: P1, p2: P2): ZIO[SlinqPg, SQLException, List[R]] =
      db.query(sur.render(p1, p2))

    def runHead(p1: P1, p2: P2): ZIO[SlinqPg, SQLException, R] =
      db.queryHead(sur.render(p1, p2))

    def runHeadOpt(p1: P1, p2: P2): ZIO[SlinqPg, SQLException, Option[R]] =
      db.queryHeadOpt(sur.render(p1, p2))

    def runAs[T](p1: P1, p2: P2)(
      using transform: R => T
    ): ZIO[SlinqPg, SQLException, List[T]] =
      db.queryAs(sur.render(p1, p2), transform)

    def runHeadAs[T](p1: P1, p2: P2)(
      using transform: R => T
    ): ZIO[SlinqPg, SQLException, T] =
      db.queryHeadAs(sur.render(p1, p2), transform)

    def runHeadOptAs[T](p1: P1, p2: P2)(
      using transform: R => T
    ): ZIO[SlinqPg, SQLException, Option[T]] =
      db.queryHeadOptAs(sur.render(p1, p2), transform)
  }

  // delete

  extension [P](sd: StoredDelete[P]) {

    def run(params: P): ZIO[SlinqPg, SQLException, Unit] =
      db.exec(sd.render(params))

    def runNum(params: P): ZIO[SlinqPg, SQLException, Int] =
      db.execNum(sd.render(params))

    def runList(paramList: Seq[P]): ZIO[SlinqPg, SQLException, Unit] =
      db.execList(paramList.map(sd.render(_)))

    def collect(size: Int): ZSink[Any, Nothing, P, P, Chunk[P]] =
      ZSink.collectAllN[P](size)

    def asSink: ZSink[SlinqPg, SQLException, P, Nothing, Unit] =
      ZSink.foreach((params: P) => db.exec(sd.render(params)))

    def asListSink: ZSink[SlinqPg, SQLException, Chunk[P], Nothing, Unit] =
      ZSink.foreach { (chunk: Chunk[P]) =>
        db.execList(chunk.toList.map(p => sd.render(p)))
      }
  }
}
