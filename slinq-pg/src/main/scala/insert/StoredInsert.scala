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

import scala.deriving.Mirror
import slinq.pg.shape.{ParamConv, RowConv}
import slinq.pg.run.RunQueryParams
import slinq.pg.render.{RenderedOperation, RenderedQuery, JoinArgs}

class StoredInsert[P](
  val statement: String,
  val args: Vector[Any],
  paramConv: ParamConv[P]
) extends JoinArgs {

  def render(params: P) = RenderedOperation(
    statement,
    joinArgs(args, paramConv.fromShape(params))
  )

  def printSql: StoredInsert[P] = {
    println(statement)
    this
  }

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

// Typed version with RETURNING clause that converts case class to tuple automatically
class StoredInsertReturningTyped[T <: Product, P, R](
  val statement: String,
  args: Vector[Any],
  paramConv: ParamConv[P],
  rowConv: RowConv[R]
)(using mirror: Mirror.ProductOf[T] { type MirroredElemTypes = P })
    extends JoinArgs {

  def render(value: T) = {
    val tup = Tuple.fromProductTyped(value)(using mirror.asInstanceOf[Mirror.ProductOf[T]])
    RenderedQuery(
      statement,
      joinArgs(args, paramConv.fromShape(tup.asInstanceOf[P])),
      rowConv
    )
  }

  def printSql: StoredInsertReturningTyped[T, P, R] = {
    println(statement)
    this
  }

  def printSqlAndArgs(value: T): StoredInsertReturningTyped[T, P, R] =
    render(value).printStatementAndArgs(this)

  def printSqlWithArgs(value: T): StoredInsertReturningTyped[T, P, R] =
    render(value).printStatementWithArgs(this)

}
