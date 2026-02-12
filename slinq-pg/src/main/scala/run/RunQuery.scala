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

package slinq.pg.run

import slinq.pg.render.{RenderedQuery, JoinArgs}

trait RunQuery[R] {

  def render: RenderedQuery[R]

  def printSql: RunQuery[R] =
    render.printStatement(this)

  def printSqlAndArgs: RunQuery[R] =
    render.printStatementAndArgs(this)

  def printSqlWithArgs: RunQuery[R] =
    render.printStatementWithArgs(this)

}

trait RunQueryParams[P, R] extends JoinArgs {

  val statement: String

  def render(params: P): RenderedQuery[R]

  def printSql: RunQueryParams[P, R] = {
    println(statement)
    this
  }

  def printSqlAndArgs(params: P): RunQueryParams[P, R] =
    render(params).printStatementAndArgs(this)

  def printSqlWithArgs(params: P): RunQueryParams[P, R] =
    render(params).printStatementWithArgs(this)

}
