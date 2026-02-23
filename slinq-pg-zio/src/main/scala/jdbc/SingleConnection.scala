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

package slinq.pg.zio.jdbc

import java.sql.DriverManager
import java.sql.SQLException

import _root_.zio.*

import slinq.pg.api.PgConfig
import slinq.pg.jdbc.JdbcMethods
import slinq.pg.render.{RenderedQuery, RenderedOperation}

class SingleConnection(conf: PgConfig) extends JdbcMethods {

  val conn = DriverManager.getConnection(conf.url, conf.props)

  def query[R](stm: RenderedQuery[R]): IO[SQLException, List[R]] =
    ZIO.attemptBlocking(runQuery(stm)).refineToOrDie[SQLException]

  def exec(stm: RenderedOperation): IO[SQLException, Unit] =
    ZIO.attemptBlocking(runExec(stm)).unit.refineToOrDie[SQLException]

  def execNum(stm: RenderedOperation): IO[SQLException, Int] =
    ZIO
      .attemptBlocking(runExecNum(stm))
      .refineToOrDie[SQLException]

  def execList(stms: Seq[RenderedOperation]): IO[SQLException, Unit] =
    ZIO.attemptBlocking(runExecList(stms)).unit.refineToOrDie[SQLException]

  def isValid: UIO[Boolean] =
    ZIO.attemptBlocking(conn.isValid(10)).catchAll(_ => ZIO.succeed(false))

  def close: UIO[Unit] = ZIO.attemptBlocking(conn.close()).orDie
}
