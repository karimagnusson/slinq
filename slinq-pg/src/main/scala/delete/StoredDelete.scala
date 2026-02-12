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

package slinq.pg.delete

import slinq.pg.shape.{RowConv, ParamConv}
import slinq.pg.run.RunQueryParams
import slinq.pg.render.{RenderedOperation, RenderedQuery, JoinArgs}

class StoredDelete[P](
  val statement: String,
  args: Vector[Any],
  paramConv: ParamConv[P]
) extends JoinArgs {

  def render(params: P) = RenderedOperation(
    statement,
    joinArgs(args, paramConv.fromShape(params))
  )

  def printSql: StoredDelete[P] = {
    println(statement)
    this
  }

  def printSqlAndArgs(params: P): StoredDelete[P] =
    render(params).printStatementAndArgs(this)

  def printSqlWithArgs(params: P): StoredDelete[P] =
    render(params).printStatementWithArgs(this)

}

class StoredDeleteReturning[P, R](
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
