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

package slinq.pg.fn

import slinq.pg.column.TypeCol
import slinq.pg.fn.types.*


object Cast {
  def asString(col: TypeCol[?]) = CastString(col)
  def asShort(col: TypeCol[?]) = CastShort(col)
  def asInt(col: TypeCol[?]) = CastInt(col)
  def asLong(col: TypeCol[?]) = CastLong(col)
  def asFloat(col: TypeCol[?]) = CastFloat(col)
  def asDouble(col: TypeCol[?]) = CastDouble(col)
  def asBigDecimal(col: TypeCol[?]) = CastBigDecimal(col)
  def asJsonb(col: TypeCol[?]) = CastJsonb(col)
  def asTimestamp(col: TypeCol[?]) = CastTimestamp(col)
  def asDate(col: TypeCol[?]) = CastDate(col)
  def asTime(col: TypeCol[?]) = CastTime(col)
}





