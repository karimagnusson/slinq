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

import slinq.pg.column.TypeCol


trait RowShape[R] {
  val cols: Vector[TypeCol[?]]
  val conv: RowConv[R]
}


case class RowShapeSingle[R](col: TypeCol[R]) extends RowShape[R] {
  val cols = Vector(col)
  val conv = new RowConvSingle(col.conv)
}


// Type-level function to extract result types from tuple of TypeCol
type ExtractColTypes[T <: Tuple] <: Tuple = T match {
  case TypeCol[r] *: EmptyTuple => r *: EmptyTuple
  case TypeCol[r] *: tail => r *: ExtractColTypes[tail]
  case EmptyTuple => EmptyTuple
}

// Type-level function to extract ValConv types from tuple of TypeCol
type ExtractConvTuple[T <: Tuple] <: Tuple = T match {
  case TypeCol[r] *: EmptyTuple => Tuple1[slinq.pg.conv.ValConv[r]]
  case TypeCol[r] *: tail => slinq.pg.conv.ValConv[r] *: ExtractConvTuple[tail]
  case EmptyTuple => EmptyTuple
}

// Single polymorphic RowShape for all tuple sizes
class RowShapeTupled[T <: Tuple](shape: T) extends RowShape[ExtractColTypes[T]] {

  val cols: Vector[TypeCol[?]] = {
    shape.toList.asInstanceOf[List[TypeCol[?]]].toVector
  }

  val conv: RowConv[ExtractColTypes[T]] = {
    val convs = shape.toList.asInstanceOf[List[TypeCol[Any]]].map(_.conv)
    new RowConvTupled(Tuple.fromArray(convs.toArray).asInstanceOf[ExtractConvTuple[T]]).asInstanceOf[RowConv[ExtractColTypes[T]]]
  }
}


class RowShapeSeq(val picked: Seq[TypeCol[?]]) extends RowShape[Seq[Any]] {
  val cols = picked.toVector
  val conv = new RowConvSeq(cols.map(_.conv).toVector)
}


class RowShapeNamed(
  val picked: Seq[Tuple2[String, TypeCol[?]]]
) extends RowShape[Seq[Tuple2[String, Any]]] {
  val cols = picked.map(_._2).toVector
  val conv = new RowConvNamed(picked.map(t => (t._1, t._2.conv)).toVector)
}
