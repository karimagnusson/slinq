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

import scala.deriving.Mirror
import slinq.pg.conv.ValConv

trait ParamConv[P] {
  def fromShape(params: P): Vector[Any]
}

class ParamConvSingle[P](conv: ValConv[P]) extends ParamConv[P] {
  def fromShape(param: P) = Vector(conv.put(param))
}

// Type-level function to extract param types from tuple of ValConv
type ExtractParamTypes[T <: Tuple] <: Tuple = T match {
  case ValConv[p] *: EmptyTuple => p *: EmptyTuple
  case ValConv[p] *: tail => p *: ExtractParamTypes[tail]
  case EmptyTuple => EmptyTuple
}

// Single polymorphic ParamConv for all tuple sizes
class ParamConvTupled[T <: Tuple](convs: T) extends ParamConv[ExtractParamTypes[T]] {

  def fromShape(params: ExtractParamTypes[T]): Vector[Any] = {
    val convList = convs.toList.asInstanceOf[List[ValConv[Any]]]
    val paramList = params.toList
    convList.zip(paramList).map { case (conv, param) => conv.put(param) }.toVector
  }

}


// Typed version that converts case class to tuple automatically
class ParamConvTyped[T <: Product, P](
  convs: ParamConv[P],
  mirror: Mirror.ProductOf[T]
) extends ParamConv[T] {

  def fromShape(value: T): Vector[Any] = {
    val tup = Tuple.fromProductTyped(value)(using mirror)
    convs.fromShape(tup.asInstanceOf[P])
  }
}
