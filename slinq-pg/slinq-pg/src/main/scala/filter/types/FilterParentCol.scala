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

package slinq.pg.filter.types

import slinq.pg.filter.Filter
import slinq.pg.render.{Renderable, Prefix}
import slinq.pg.api.SlinqError


case class FilterParentEqCol(col: Renderable, parCol: Renderable) extends Filter {
  val template = "%s = %s"

  def render(prefix: Prefix) =
    throw SlinqError("cannot use subquery filter")
  
  def renderParent(parPrefix: Prefix) =
    FilterParentEqRendered(col, parCol.render(parPrefix), parCol.args)

  val args = col.args ++ parCol.args
}

case class FilterParentEqRendered(col: Renderable, parRend: String, parArgs: Vector[Any]) extends Filter {
  val template = "%s = " + parRend
  def render(prefix: Prefix) = template.format(col.render(prefix))
  val args = col.args ++ parArgs
}