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

package slinq.pg.shape

import scala.reflect.ClassTag
import slinq.pg.column.TypeCol

trait ParamShape[P] {
  val size: Int
  val cols: Vector[TypeCol[?]]
  val conv: ParamConv[P]
}

class ParamShapeSingle[P](col: TypeCol[P]) extends ParamShape[P] {
  val size = 1
  val cols = Vector(col)
  val conv = new ParamConvSingle(col.conv)
}

// Type-level function to extract column types from tuple of TypeCol
type ExtractColParamTypes[T <: Tuple] <: Tuple = T match {
  case TypeCol[p] *: EmptyTuple => p *: EmptyTuple
  case TypeCol[p] *: tail => p *: ExtractColParamTypes[tail]
  case EmptyTuple => EmptyTuple
}

// Type-level function to extract ValConv types from tuple of TypeCol
type ExtractParamConvTuple[T <: Tuple] <: Tuple = T match {
  case TypeCol[p] *: EmptyTuple => Tuple1[slinq.pg.conv.ValConv[p]]
  case TypeCol[p] *: tail => slinq.pg.conv.ValConv[p] *: ExtractParamConvTuple[tail]
  case EmptyTuple => EmptyTuple
}

// Single polymorphic ParamShape for all tuple sizes
class ParamShapeTupled[T <: Tuple](shape: T) extends ParamShape[ExtractColParamTypes[T]] {

  val cols: Vector[TypeCol[?]] =
    shape.toList.asInstanceOf[List[TypeCol[?]]].toVector

  val size: Int = cols.size

  val conv: ParamConv[ExtractColParamTypes[T]] = {
    val convs = shape.toList.asInstanceOf[List[TypeCol[Any]]].map(_.conv)
    new ParamConvTupled(Tuple.fromArray(convs.toArray).asInstanceOf[ExtractParamConvTuple[T]])
      .asInstanceOf[ParamConv[ExtractColParamTypes[T]]]
  }

}
