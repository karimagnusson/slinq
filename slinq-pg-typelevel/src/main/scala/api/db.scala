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

package slinq.pg.typelevel.api

import cats.effect.IO
import slinq.pg.render.{RenderedQuery, RenderedOperation}
import slinq.pg.typelevel.jdbc.JdbcExecutor

object db {

  private var _pool: JdbcExecutor = null

  def init(pool: JdbcExecutor): Unit =
    _pool = pool

  def close(): IO[Unit] =
    if (_pool != null) _pool.close else IO.unit

  private def pool: JdbcExecutor =
    if (_pool == null) throw new RuntimeException("db not initialized")
    else _pool

  def query[R](render: => RenderedQuery[R]): IO[List[R]] =
    pool.query(render)

  def queryAs[R, T](render: => RenderedQuery[R], transform: R => T): IO[List[T]] =
    pool.query(render).map(_.map(transform))

  def queryHead[R](render: => RenderedQuery[R]): IO[R] =
    pool.query(render).map(_.head)

  def queryHeadAs[R, T](render: => RenderedQuery[R], transform: R => T): IO[T] =
    pool.query(render).map(_.head).map(transform)

  def queryHeadOpt[R](render: => RenderedQuery[R]): IO[Option[R]] =
    pool.query(render).map(_.headOption)

  def queryHeadOptAs[R, T](render: => RenderedQuery[R], transform: R => T): IO[Option[T]] =
    pool.query(render).map(_.headOption.map(transform))

  def exec(render: => RenderedOperation): IO[Unit] =
    pool.exec(render)

  def execNum(render: => RenderedOperation): IO[Int] =
    pool.execNum(render)

  def execList(stms: Seq[RenderedOperation]): IO[Unit] =
    pool.execList(stms)
}
