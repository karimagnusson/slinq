error id: file://<WORKSPACE>/slinq-pg/src/main/scala/api/SlinqPg.scala:`<none>`.
file://<WORKSPACE>/slinq-pg/src/main/scala/api/SlinqPg.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -slinq/pg/render/RenderedOperation#
	 -zio/RenderedOperation#
	 -RenderedOperation#
	 -scala/Predef.RenderedOperation#
offset: 3221
uri: file://<WORKSPACE>/slinq-pg/src/main/scala/api/SlinqPg.scala
text:
```scala
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

package slinq.pg.api

import java.sql.SQLException
import scala.annotation.nowarn
import slinq.pg.jdbc.SingleConnection
import slinq.pg.render.{
  RenderedQuery,
  RenderedOperation
}

import zio._


object SlinqPg {

  Class.forName("org.postgresql.Driver")

  private def makeConn(conf: PgConfig): IO[SQLException, SingleConnection] = ZIO.attempt {
    new SingleConnection(conf)
  }.refineToOrDie[SQLException]

  private def create(conf: PgConfig): ZIO[Scope, Nothing, ZPool[SQLException, SingleConnection]] = {
    val getConn = ZIO.acquireRelease(makeConn(conf))(_.close)
    ZPool.make(getConn, Range(conf.minPoolSize, conf.poolSize), 300.seconds)
  }

  def layer(conf: PgConfig): ZLayer[Any, SQLException, SlinqPg] = {
    ZLayer.scoped {
      for {
        pool <- create(conf)
      } yield new DefaultApi(new Pool(pool))
    }
  }
  
  def get = ZIO.service[SlinqPg]
}


trait SlinqPg {

  def query[R](render: => RenderedQuery[R]): Task[List[R]]

  def queryAs[R, T](render: => RenderedQuery[R], transform: R => T): Task[List[T]] =
    query(render).map(_.map(transform))

  def queryHead[R](render: => RenderedQuery[R]): Task[R] = 
    query(render).map(_.headOption.getOrElse(throw NoRowsException("Query returned no rows")))

  def queryHeadAs[R, T](render: => RenderedQuery[R], transform: R => T): Task[T] =
    queryHead(render).map(transform)

  def queryHeadOpt[R](render: => RenderedQuery[R]): Task[Option[R]] =
    query(render).map(_.headOption)

  def queryHeadOptAs[R, T](render: => RenderedQuery[R], transform: R => T): Task[Option[T]] =
    query(render).map(_.headOption.map(transform))

  def exec(render: => RenderedOperation): Task[Unit]

  def execNum(render: => RenderedOperation): Task[Int]

  def execList(stms: Seq[RenderedOperation]): Task[Unit]
}


private class Pool(pool: ZPool[SQLException, SingleConnection]) {

  @nowarn
  def use[R](fn: SingleConnection => IO[SQLException, R]): IO[SQLException, R] = ZIO.scoped {
    for {
      conn  <- pool.get
      _     <- ZIO.addFinalizerExit {
        case Exit.Success(_) =>
          ZIO.succeed(())
        case Exit.Failure(_) => 
          ZIO.unlessZIO(conn.isValid)(pool.invalidate(conn))
      }
      res   <- fn(conn)
    } yield res
  }.refineToOrDie[SQLException]
}


private class DefaultApi(pool: Pool) extends SlinqPg {

  def query[R](render: => RenderedQuery[R]) = for {
    stm     <- ZIO.attempt(render)
    rows    <- pool.use(_.query(stm))
  } yield rows

  def exec(render: => RenderedOperation) = for {
    stm     <- ZIO.attempt(render)
    _       <- pool.use(_.exec(stm))
  } yield ()

  def execNum(render: => RenderedOper@@ation) = for {
    stm     <- ZIO.attempt(render)
    num     <- pool.use(_.execNum(stm))
  } yield num

  def execList(stms: Seq[RenderedOperation]) = for {
    _       <- pool.use(_.execList(stms))
  } yield ()
}


private class SplitApi(getPool: Pool, setPool: Pool) extends SlinqPg {

  private def router(stm: String) = stm.substring(0, 6).toUpperCase match {
    case "SELECT" => getPool
    case _ => setPool
  }

  def query[R](render: => RenderedQuery[R]) = for {
    stm     <- ZIO.attempt(render)
    rows    <- router(stm.statement).use(_.query(stm))
  } yield rows

  def exec(render: => RenderedOperation) = for {
    stm     <- ZIO.attempt(render)
    _       <- setPool.use(_.exec(stm))
  } yield ()

  def execNum(render: => RenderedOperation) = for {
    stm     <- ZIO.attempt(render)
    num     <- setPool.use(_.execNum(stm))
  } yield num

  def execList(stms: Seq[RenderedOperation]) = for {
    _       <- setPool.use(_.execList(stms))
  } yield ()
}

























```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.