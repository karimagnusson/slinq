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

package slinq.pg.insert

import java.sql.SQLException
import scala.deriving.Mirror.ProductOf
import slinq.pg.api.{db, SlinqPg}
import slinq.pg.shape.{ParamConv, RowConv}
import slinq.pg.run.RunQueryParams
import slinq.pg.render.{
  RenderedOperation,
  RenderedQuery,
  JoinArgs
}
import zio._
import zio.stream.ZSink


class StoredInsert[P](
  val statement: String,
  args: Vector[Any],
  paramConv: ParamConv[P]
) extends JoinArgs {

  def render(params: P) = RenderedOperation(
    statement,
    joinArgs(args, paramConv.fromShape(params))
  )

  def run(params: P): ZIO[SlinqPg, SQLException, Unit] =
    db.exec(render(params))

  def runNum(params: P): ZIO[SlinqPg, SQLException, Int] =
    db.execNum(render(params))

  def runList(paramList: Seq[P]): ZIO[SlinqPg, SQLException, Unit] =
    db.execList(paramList.map(render(_)))

  def runType[T <: Product](value: T)(
    using mirror: ProductOf[T],
          ev: P <:< mirror.MirroredElemTypes
  ): ZIO[SlinqPg, SQLException, Unit] = {
    run(Tuple.fromProductTyped(value).asInstanceOf[P])
  }

  def runNumType[T <: Product](value: T)(
    using mirror: ProductOf[T],
          ev: P <:< mirror.MirroredElemTypes
  ): ZIO[SlinqPg, SQLException, Int] = {
    runNum(Tuple.fromProductTyped(value).asInstanceOf[P])
  }

  def runListType[T <: Product](values: Seq[T])(
    using mirror: ProductOf[T],
          ev: P <:< mirror.MirroredElemTypes
  ): ZIO[SlinqPg, SQLException, Unit] = {
    runList(
      values.map(v => Tuple.fromProductTyped(v).asInstanceOf[P])
    )
  }

  def collect(size: Int): ZSink[Any, Nothing, P, P, Chunk[P]] =
    ZSink.collectAllN[P](size)

  def asSink: ZSink[SlinqPg, SQLException, P, Nothing, Unit] =
    ZSink.foreach((params: P) => db.exec(render(params)))

  def asChunkSink: ZSink[SlinqPg, SQLException, Chunk[P], Nothing, Unit] = {
    ZSink.foreach { (chunk: Chunk[P]) =>
      db.execList(chunk.toList.map(p => render(p)))
    }
  }

  def asTypeSink[T <: Product](
    using mirror: ProductOf[T],
          ev: P <:< mirror.MirroredElemTypes
  ): ZSink[SlinqPg, SQLException, T, Nothing, Unit]= {
    ZSink.foreach { (t: T) => 
      db.exec(
        render(
          Tuple.fromProductTyped(t).asInstanceOf[P]
        )
      )
    }
  }

  def asTypeChunkSink[T <: Product](
    using mirror: ProductOf[T],
          ev: P <:< mirror.MirroredElemTypes
  ): ZSink[SlinqPg, SQLException, Chunk[T], Nothing, Unit]= {
    ZSink.foreach { (chunk: Chunk[T]) =>
      db.execList(
        chunk.toList.map { t =>
          render(
            Tuple.fromProductTyped(t).asInstanceOf[P]
          )
        }
      )
    }
  }

  def printSql: StoredInsert[P] = {
    println(statement)
    this
  }
  
  def printSqlAndArgs(params: P): StoredInsert[P] =
    render(params).printStatementAndArgs(this)
  
  def printSqlWithArgs(params: P): StoredInsert[P] =
    render(params).printStatementWithArgs(this)
}


class StoredInsertReturning[P, R](
  val statement: String,
  args: Vector[Any],
  paramConv: ParamConv[P],
  rowConv: RowConv[R]
) extends RunQueryParams[P, R] {

  def render(params: P) = RenderedQuery(
    statement,
    joinArgs(args, paramConv.fromShape(params)),
    rowConv
  )
}






