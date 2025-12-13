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

package slinq.pg.filter

import java.sql.Time
import org.postgresql.util.PGInterval
import slinq.pg.column.TypeCol
import slinq.pg.assign._
import slinq.pg.conv._
import slinq.pg.fn.types._
import slinq.pg.fn.Cast
import slinq.pg.api.{Arg, NoArg}


trait TimeMethods extends ComparativeMethods[Time] with InMethods[Time] {

  // filters

  def +(value: PGInterval) = DateTimeIncFn(col, value)
  def -(value: PGInterval) = DateTimeDecFn(col, value)

  def hour = ExtractHourFn(col)
  def minute = ExtractMinuteFn(col)
  def second = ExtractSecondFn(col)
  def milliseconds = ExtractMillisecondsFn(col)
  def microseconds = ExtractMicrosecondsFn(col)

  def asString = Cast.asString(col)

  def format(value: String) = DateTimeFormatFn(col, value)

  // update

  def setNow = TimeNow(col)
  def +=(value: PGInterval) = DateTimeInc(col, value)
  def -=(value: PGInterval) = DateTimeDec(col, value)

  // cache

  def use = TimeCache(col)
}


case class TimeCache(col: TypeCol[Time]) extends TypeCache[Time]
                                            with InCache[Time] {

  def setNow(arg: NoArg) = CacheSetTimeNow(col, NoArgConv)
  def +=(arg: Arg) = CacheDateTimeInc(col, IntervalConv)
  def -=(arg: Arg) = CacheDateTimeDec(col, IntervalConv)
}












