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

package slinq.pg.fn.types

import slinq.pg.column.*


trait ExtractFn extends IntCol with SingleColFn {
  val field: String
  def template = s"EXTRACT($field FROM %s)::int"
}

case class ExtractCenturyFn(col: TypeCol[?]) extends ExtractFn {
  val field = "CENTURY"
}

case class ExtractDayFn(col: TypeCol[?]) extends ExtractFn {
  val field = "DAY"
}

case class ExtractDecadeFn(col: TypeCol[?]) extends ExtractFn {
  val field = "DECADE"
}

case class ExtractDowFn(col: TypeCol[?]) extends ExtractFn {
  val field = "DOW"
}

case class ExtractDoyFn(col: TypeCol[?]) extends ExtractFn {
  val field = "DOY"
}

case class ExtractHourFn(col: TypeCol[?]) extends ExtractFn {
  val field = "HOUR"
}

case class ExtractIsoDowFn(col: TypeCol[?]) extends ExtractFn {
  val field = "ISODOW"
}

case class ExtractIsoYearFn(col: TypeCol[?]) extends ExtractFn {
  val field = "ISOYEAR"
}

case class ExtractMicrosecondsFn(col: TypeCol[?]) extends ExtractFn {
  val field = "MICROSECONDS"
}

case class ExtractMillisecondsFn(col: TypeCol[?]) extends ExtractFn {
  val field = "MILLISECONDS"
}

case class ExtractMinuteFn(col: TypeCol[?]) extends ExtractFn {
  val field = "MINUTE"
}

case class ExtractMonthFn(col: TypeCol[?]) extends ExtractFn {
  val field = "MONTH"
}

case class ExtractQuarterFn(col: TypeCol[?]) extends ExtractFn {
  val field = "QUARTER"
}

case class ExtractSecondFn(col: TypeCol[?]) extends ExtractFn {
  val field = "SECOND"
}

case class ExtractWeekFn(col: TypeCol[?]) extends ExtractFn {
  val field = "WEEK"
}

case class ExtractYearFn(col: TypeCol[?]) extends ExtractFn {
  val field = "YEAR"
}

case class ExtractEpochSecsFn(col: TypeCol[?]) extends LongCol with SingleColFn {
  val template = "EXTRACT(EPOCH FROM %s)::bigint"
}

case class ExtractEpochMillisFn(col: TypeCol[?]) extends LongCol with SingleColFn {
  val template = "(EXTRACT(EPOCH FROM %s) * 1000)::bigint"
}

// cast




















