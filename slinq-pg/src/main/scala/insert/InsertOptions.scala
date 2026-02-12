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
import slinq.pg.api.Model
import slinq.pg.column.TypeCol
import slinq.pg.select.{Subquery, SubqueryInsertFc}
import slinq.pg.shape.ParamConvTyped

class InsertOptions[M <: Model, P](
  builder: InsertBuilder[M, P]
) extends PickStoredInsertReturning(builder) {

  // no cache

  def values(params: P) = new Values(
    builder.toValuesBuilder(params)
  )

  def fromSelect(sub: Subquery[P]) = new RenderInsert(
    builder.fromSelect(sub)
  )

  // cache

  def cache = {
    val coll = builder.collector
    new StoredInsert(
      coll.render,
      coll.args,
      builder.paramShape.conv
    )
  }

  def cacheTyped[T <: Product](using m: Mirror.ProductOf[T] { type MirroredElemTypes = P }) = {
    val coll = builder.collector
    val typedConv = new ParamConvTyped[T, P](builder.paramShape.conv, m.asInstanceOf[Mirror.ProductOf[T]])
    new StoredInsert[T](
      coll.render,
      coll.args,
      typedConv
    )
  }

  def pickSelect[R](sub: SubqueryInsertFc[R, P]) =
    new RenderStoredInsert(
      builder.fromSelect(sub),
      sub.paramConv
    )

  def whereNotExists(pick: M => Seq[TypeCol[?]]) = {
    val uniqueCols = pick(builder.model).toVector
    new RenderStoredInsert(
      builder.whereNotExists(uniqueCols),
      builder.whereNotExistsReuse(uniqueCols)
    )
  }

  // on conflict

  def onConflictDoNothing =
    new RenderStoredInsert(
      builder.onConflictDoNothing,
      builder.paramShape.conv
    )

  def onConflictOnColumn(pick: M => TypeCol[?]) =
    new DoUpdateStored(
      builder,
      pick(builder.model)
    )

}
