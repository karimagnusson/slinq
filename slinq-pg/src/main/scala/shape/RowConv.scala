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

import java.sql.ResultSet
import scala.util.{Try, Success, Failure}
import scala.reflect.{classTag, ClassTag}
import slinq.pg.api.SlinqError
import slinq.pg.conv.ValConv


trait RowConv[R] {
  def fromRow(rs: ResultSet): R
}


class RowConvSingle[R](val col: ValConv[R]) extends RowConv[R] {
  def fromRow(rs: ResultSet) = col.get(rs, 1)
}


// Type-level function to extract result types from tuple of ValConv
type ExtractConvTypes[T <: Tuple] <: Tuple = T match {
  case ValConv[r] *: EmptyTuple => r *: EmptyTuple
  case ValConv[r] *: tail => r *: ExtractConvTypes[tail]
  case EmptyTuple => EmptyTuple
}

// Single polymorphic RowConv for all tuple sizes
class RowConvTupled[T <: Tuple](shape: T) extends RowConv[ExtractConvTypes[T]] {

  def fromRow(rs: ResultSet): ExtractConvTypes[T] = {
    val converters = shape.toList.asInstanceOf[List[ValConv[Any]]]
    val values = converters.zipWithIndex.map { case (conv, idx) =>
      conv.get(rs, idx + 1)
    }
    Tuple.fromArray(values.toArray).asInstanceOf[ExtractConvTypes[T]]
  }
}


class RowConvSeq(val cols: Vector[ValConv[?]]) extends RowConv[Seq[Any]] {

  private val indexedCols = cols.zipWithIndex.map(p => (p._1, p._2 + 1))

  def fromRow(rs: ResultSet) = {
    indexedCols.toVector.map {
      case (col, index) =>
        col.get(rs, index)
    }
  }
}


class RowConvNamed(
  val cols: Vector[Tuple2[String, ValConv[?]]]
) extends RowConv[Seq[Tuple2[String, Any]]] {

  private val indexedCols = cols.zipWithIndex.map {
    case ((name, col), index) => (name, col, index + 1)
  }

  def fromRow(rs: ResultSet) = {
    indexedCols.toVector.map {
      case (name, col, index) =>
        (name, col.get(rs, index))
    }
  }
}


class RowConvReader[R](val cols: Vector[ValConv[?]])(implicit tag: ClassTag[R]) extends RowConv[R] {

  private val indexedCols = cols.zipWithIndex.map(p => (p._1, p._2 + 1))

  private def read(rs: ResultSet): Seq[AnyRef] = {
    indexedCols.map {
      case (col, index) =>
        col.get(rs, index).asInstanceOf[AnyRef]
    }
  }

  def fromRow(rs: ResultSet) = {
    Try {

      classTag[R]
        .runtimeClass
        .getConstructors
        .head
        .newInstance(read(rs) *)
        .asInstanceOf[R]

    } match {
      case Success(res) => res
      case Failure(ex) =>
        val name = classTag[R].runtimeClass.getName
        val message = ex.getMessage
        throw SlinqError(
          s"Failed to read ($name) $message"
        )
    }
  }
}


object RowConvRaw extends RowConv[Vector[Any]] {

  def fromRow(rs: ResultSet) = {
    (1 to rs.getMetaData.getColumnCount).toVector.map { index =>
      rs.getObject(index)
    }
  }
}
