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

package slinq.pg.typelevel.jdbc

import java.sql.Connection

import cats.effect.IO
import com.zaxxer.hikari.HikariDataSource
import slinq.pg.jdbc.JdbcMethods
import slinq.pg.render.{RenderedQuery, RenderedOperation}

class SingleConnection(val conn: Connection) extends JdbcMethods

class JdbcExecutor(pool: HikariDataSource) {

  def query[R](stm: RenderedQuery[R]): IO[List[R]] =
    IO.blocking {
      val conn = pool.getConnection()
      try {
        new SingleConnection(conn).runQuery(stm)
      } finally {
        conn.close()
      }
    }

  def exec(stm: RenderedOperation): IO[Unit] =
    IO.blocking {
      val conn = pool.getConnection()
      try {
        new SingleConnection(conn).runExec(stm)
      } finally {
        conn.close()
      }
    }

  def execNum(stm: RenderedOperation): IO[Int] =
    IO.blocking {
      val conn = pool.getConnection()
      try {
        new SingleConnection(conn).runExecNum(stm)
      } finally {
        conn.close()
      }
    }

  def execList(stms: Seq[RenderedOperation]): IO[Unit] =
    IO.blocking {
      val conn = pool.getConnection()
      try {
        new SingleConnection(conn).runExecList(stms)
      } finally {
        conn.close()
      }
    }

  def close: IO[Unit] =
    IO.blocking(pool.close())

}
