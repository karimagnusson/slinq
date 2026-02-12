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
import java.sql.Date
import java.sql.Timestamp
import java.util.UUID

import slinq.pg.column.TypeCol
import slinq.pg.api.Jsonb

trait filters {

  given slinqStringConversion: Conversion[TypeCol[String], StringMethods] =
    (c: TypeCol[String]) => new StringMethods { val col = c }

  given slinqBooleanConversion: Conversion[TypeCol[Boolean], BooleanMethods] =
    (c: TypeCol[Boolean]) => new BooleanMethods { val col = c }

  given slinqShortNumericConversion: Conversion[TypeCol[Short], NumericMethods[Short]] =
    (c: TypeCol[Short]) => new NumericMethods[Short] { val col = c }

  given slinqIntNumericConversion: Conversion[TypeCol[Int], NumericMethods[Int]] =
    (c: TypeCol[Int]) => new NumericMethods[Int] { val col = c }

  given slinqLongNumericConversion: Conversion[TypeCol[Long], NumericMethods[Long]] =
    (c: TypeCol[Long]) => new NumericMethods[Long] { val col = c }

  given slinqFloatNumericConversion: Conversion[TypeCol[Float], NumericMethods[Float]] =
    (c: TypeCol[Float]) => new NumericMethods[Float] { val col = c }

  given slinqDoubleNumericConversion: Conversion[TypeCol[Double], NumericMethods[Double]] =
    (c: TypeCol[Double]) => new NumericMethods[Double] { val col = c }

  given slinqBigDecimalNumericConversion: Conversion[TypeCol[BigDecimal], NumericMethods[BigDecimal]] =
    (c: TypeCol[BigDecimal]) => new NumericMethods[BigDecimal] { val col = c }

  given slinqTimeConversion: Conversion[TypeCol[Time], TimeMethods] =
    (c: TypeCol[Time]) => new TimeMethods { val col = c }

  given slinqDateConversion: Conversion[TypeCol[Date], DateMethods] =
    (c: TypeCol[Date]) => new DateMethods { val col = c }

  given slinqTimestampConversion: Conversion[TypeCol[Timestamp], TimestampMethods] =
    (c: TypeCol[Timestamp]) => new TimestampMethods { val col = c }

  given slinqJsonbConversion: Conversion[TypeCol[Jsonb], JsonbMethods] =
    (c: TypeCol[Jsonb]) => new JsonbMethods { val col = c }

  given slinqUuidTypeConversion: Conversion[TypeCol[UUID], TypeMethods[UUID]] =
    (c: TypeCol[UUID]) => new TypeMethods[UUID] { val col = c }

  given slinqStringSeqConversion: Conversion[TypeCol[Seq[String]], SeqMethods[String]] =
    (c: TypeCol[Seq[String]]) => new SeqMethods[String] { val col = c }

  given slinqBooleanSeqConversion: Conversion[TypeCol[Seq[Boolean]], SeqMethods[Boolean]] =
    (c: TypeCol[Seq[Boolean]]) => new SeqMethods[Boolean] { val col = c }

  given slinqShortSeqConversion: Conversion[TypeCol[Seq[Short]], SeqMethods[Short]] =
    (c: TypeCol[Seq[Short]]) => new SeqMethods[Short] { val col = c }

  given slinqIntSeqConversion: Conversion[TypeCol[Seq[Int]], SeqMethods[Int]] =
    (c: TypeCol[Seq[Int]]) => new SeqMethods[Int] { val col = c }

  given slinqLongSeqConversion: Conversion[TypeCol[Seq[Long]], SeqMethods[Long]] =
    (c: TypeCol[Seq[Long]]) => new SeqMethods[Long] { val col = c }

  given slinqFloatSeqConversion: Conversion[TypeCol[Seq[Float]], SeqMethods[Float]] =
    (c: TypeCol[Seq[Float]]) => new SeqMethods[Float] { val col = c }

  given slinqDoubleSeqConversion: Conversion[TypeCol[Seq[Double]], SeqMethods[Double]] =
    (c: TypeCol[Seq[Double]]) => new SeqMethods[Double] { val col = c }

  given slinqBigDecimalSeqConversion: Conversion[TypeCol[Seq[BigDecimal]], SeqMethods[BigDecimal]] =
    (c: TypeCol[Seq[BigDecimal]]) => new SeqMethods[BigDecimal] { val col = c }

  given slinqTimeSeqConversion: Conversion[TypeCol[Seq[Time]], SeqMethods[Time]] =
    (c: TypeCol[Seq[Time]]) => new SeqMethods[Time] { val col = c }

  given slinqDateSeqConversion: Conversion[TypeCol[Seq[Date]], SeqMethods[Date]] =
    (c: TypeCol[Seq[Date]]) => new SeqMethods[Date] { val col = c }

  given slinqTimestampSeqConversion: Conversion[TypeCol[Seq[Timestamp]], SeqMethods[Timestamp]] =
    (c: TypeCol[Seq[Timestamp]]) => new SeqMethods[Timestamp] { val col = c }

  given slinqJsonbSeqConversion: Conversion[TypeCol[Seq[Jsonb]], JsonbSeqMethods] =
    (c: TypeCol[Seq[Jsonb]]) => new JsonbSeqMethods { val col = c }
}
