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

package slinq.pg

import scala.concurrent.{Future, ExecutionContext}

import slinq.pg.update.{StoredUpdate, StoredUpdateReturning}
import slinq.pg.delete.StoredDelete
import slinq.pg.insert.StoredInsert
import slinq.pg.render.*
import slinq.pg.run.*
import slinq.pg.api.conversions

package object api extends conversions {

  // run query

  extension [R](rq: RunQuery[R]) {

    def run(implicit ec: ExecutionContext): Future[List[R]] =
      db.query(rq.render)

    def runHead(implicit ec: ExecutionContext): Future[R] =
      db.queryHead(rq.render)

    def runHeadOpt(implicit ec: ExecutionContext): Future[Option[R]] =
      db.queryHeadOpt(rq.render)

    def runAs[T](
      using transform: R => T
    )(implicit ec: ExecutionContext): Future[List[T]] =
      db.queryAs(rq.render, transform)

    def runHeadAs[T](
      using transform: R => T
    )(implicit ec: ExecutionContext): Future[T] =
      db.queryHeadAs(rq.render, transform)

    def runHeadOptAs[T](
      using transform: R => T
    )(implicit ec: ExecutionContext): Future[Option[T]] =
      db.queryHeadOptAs(rq.render, transform)

  }

  // run query with params

  extension [P, R](rq: RunQueryParams[P, R]) {

    def run(params: P)(implicit ec: ExecutionContext): Future[List[R]] =
      db.query(rq.render(params))

    def runHead(params: P)(implicit ec: ExecutionContext): Future[R] =
      db.queryHead(rq.render(params))

    def runHeadOpt(params: P)(implicit ec: ExecutionContext): Future[Option[R]] =
      db.queryHeadOpt(rq.render(params))

    def runAs[T](params: P)(
      using transform: R => T
    )(implicit ec: ExecutionContext): Future[List[T]] =
      db.queryAs(rq.render(params), transform)

    def runHeadAs[T](params: P)(
      using transform: R => T
    )(implicit ec: ExecutionContext): Future[T] =
      db.queryHeadAs(rq.render(params), transform)

    def runHeadOptAs[T](params: P)(
      using transform: R => T
    )(implicit ec: ExecutionContext): Future[Option[T]] =
      db.queryHeadOptAs(rq.render(params), transform)

  }

  // run operation

  extension (ro: RunOperation) {

    def run(implicit ec: ExecutionContext): Future[Unit] =
      db.exec(ro.render)

    def runNum(implicit ec: ExecutionContext): Future[Int] =
      db.execNum(ro.render)
  }

  // transaction

  extension (t: Transaction) {

    def run(implicit ec: ExecutionContext): Future[Unit] =
      db.execList(t.stms)
  }

  // insert

  extension [P](si: StoredInsert[P]) {

    def run(params: P)(implicit ec: ExecutionContext): Future[Unit] =
      db.exec(si.render(params))

    def runNum(params: P)(implicit ec: ExecutionContext): Future[Int] =
      db.execNum(si.render(params))

    def runList(paramList: Seq[P])(implicit ec: ExecutionContext): Future[Unit] =
      db.execList(paramList.map(si.render(_)))

  }

  // update

  extension [P1, P2](su: StoredUpdate[P1, P2]) {

    def run(p1: P1, p2: P2)(implicit ec: ExecutionContext): Future[Unit] =
      db.exec(su.render(p1, p2))

    def runNum(p1: P1, p2: P2)(implicit ec: ExecutionContext): Future[Int] =
      db.execNum(su.render(p1, p2))

    def runList(list: Seq[Tuple2[P1, P2]])(implicit ec: ExecutionContext): Future[Unit] =
      db.execList(list.map(p => su.render(p._1, p._2)))

  }

  extension [P1, P2, R](sur: StoredUpdateReturning[P1, P2, R]) {

    def run(p1: P1, p2: P2)(implicit ec: ExecutionContext): Future[List[R]] =
      db.query(sur.render(p1, p2))

    def runHead(p1: P1, p2: P2)(implicit ec: ExecutionContext): Future[R] =
      db.queryHead(sur.render(p1, p2))

    def runHeadOpt(p1: P1, p2: P2)(implicit ec: ExecutionContext): Future[Option[R]] =
      db.queryHeadOpt(sur.render(p1, p2))

    def runAs[T](p1: P1, p2: P2)(
      using transform: R => T
    )(implicit ec: ExecutionContext): Future[List[T]] =
      db.queryAs(sur.render(p1, p2), transform)

    def runHeadAs[T](p1: P1, p2: P2)(
      using transform: R => T
    )(implicit ec: ExecutionContext): Future[T] =
      db.queryHeadAs(sur.render(p1, p2), transform)

    def runHeadOptAs[T](p1: P1, p2: P2)(
      using transform: R => T
    )(implicit ec: ExecutionContext): Future[Option[T]] =
      db.queryHeadOptAs(sur.render(p1, p2), transform)

  }

  // delete

  extension [P](sd: StoredDelete[P]) {

    def run(params: P)(implicit ec: ExecutionContext): Future[Unit] =
      db.exec(sd.render(params))

    def runNum(params: P)(implicit ec: ExecutionContext): Future[Int] =
      db.execNum(sd.render(params))

    def runList(paramList: Seq[P])(implicit ec: ExecutionContext): Future[Unit] =
      db.execList(paramList.map(sd.render(_)))

  }

}
