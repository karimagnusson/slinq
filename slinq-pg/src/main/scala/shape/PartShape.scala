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

import slinq.pg.render.Renderable


trait PartShape[P] {
  val parts: Vector[Renderable]
  val conv: ParamConv[P]
}


class PartShapeSingle[P](val cond: CachePart[P]) extends PartShape[P] {
  val parts = Vector(cond)
  val conv = new ParamConvSingle(cond.conv)
}


// Type-level function to extract param types from tuple of CachePart
type ExtractPartParamTypes[T <: Tuple] <: Tuple = T match {
  case CachePart[p] *: EmptyTuple => p *: EmptyTuple
  case CachePart[p] *: tail => p *: ExtractPartParamTypes[tail]
  case EmptyTuple => EmptyTuple
}

// Type-level function to extract ValConv types from tuple of CachePart
type ExtractPartConvTuple[T <: Tuple] <: Tuple = T match {
  case CachePart[p] *: EmptyTuple => Tuple1[slinq.pg.conv.ValConv[p]]
  case CachePart[p] *: tail => slinq.pg.conv.ValConv[p] *: ExtractPartConvTuple[tail]
  case EmptyTuple => EmptyTuple
}

// Single polymorphic PartShape for all tuple sizes
class PartShapeTupled[T <: Tuple](shape: T) extends PartShape[ExtractPartParamTypes[T]] {

  val parts: Vector[CachePart[?]] = {
    shape.toList.asInstanceOf[List[CachePart[?]]].toVector
  }

  val conv: ParamConv[ExtractPartParamTypes[T]] = {
    val convs = shape.toList.asInstanceOf[List[CachePart[Any]]].map(_.conv)
    new ParamConvTupled(Tuple.fromArray(convs.toArray).asInstanceOf[ExtractPartConvTuple[T]])
      .asInstanceOf[ParamConv[ExtractPartParamTypes[T]]]
  }
}
