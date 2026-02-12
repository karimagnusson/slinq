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

package slinq.pg.jdbc

import java.sql.Connection

import scala.concurrent.{Future, ExecutionContext}

import com.zaxxer.hikari.HikariDataSource
import slinq.pg.render.{RenderedQuery, RenderedOperation}

class SingleConnection(val conn: Connection) extends JdbcMethods

class JdbcExecutor(pool: HikariDataSource, dbContext: ExecutionContext) {

  def query[R](stm: RenderedQuery[R]): Future[List[R]] =
    Future {
      val conn = pool.getConnection()
      try {
        new SingleConnection(conn).runQuery(stm)
      } finally {
        conn.close()
      }
    }(dbContext)

  def exec(stm: RenderedOperation): Future[Unit] =
    Future {
      val conn = pool.getConnection()
      try {
        new SingleConnection(conn).runExec(stm)
      } finally {
        conn.close()
      }
    }(dbContext)

  def execNum(stm: RenderedOperation): Future[Int] =
    Future {
      val conn = pool.getConnection()
      try {
        new SingleConnection(conn).runExecNum(stm)
      } finally {
        conn.close()
      }
    }(dbContext)

  def execList(stms: Seq[RenderedOperation]): Future[Unit] =
    Future {
      val conn = pool.getConnection()
      try {
        new SingleConnection(conn).runExecList(stms)
      } finally {
        conn.close()
      }
    }(dbContext)

  def close =
    pool.close()

}
