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

package slinq.pg.jdbc

import java.util.UUID
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Time
import java.sql.Date
import java.sql.Timestamp
import scala.collection.mutable.ListBuffer
import org.postgresql.util.PGInterval
import org.postgresql.util.PGobject

import slinq.pg.conv.{TypeNull, TypeArray}
import slinq.pg.api.{Jsonb, NoArg, SlinqInvalidArgException, SlinqResultTypeException}
import slinq.pg.render.{RenderedQuery, RenderedOperation}

trait JdbcMethods {

  val conn: Connection

  private val notNoArg: Any => Boolean = (arg: Any) =>
    arg.asInstanceOf[Matchable] match {
      case NoArg => false
      case _ => true
    }

  private def arrayArg(value: TypeArray) =
    conn.createArrayOf(value.typeName, value.vec.toArray)

  private def jsonbArg(value: Jsonb) = {
    val obj = new PGobject()
    obj.setType("jsonb")
    obj.setValue(value.value)
    obj
  }

  private def setArg(jdbcStm: PreparedStatement, arg: Any, index: Int): Unit =
    arg.asInstanceOf[Matchable] match {
      case value: String => jdbcStm.setString(index, value)
      case value: Boolean => jdbcStm.setBoolean(index, value)
      case value: Short => jdbcStm.setShort(index, value)
      case value: Int => jdbcStm.setInt(index, value)
      case value: Long => jdbcStm.setLong(index, value)
      case value: Float => jdbcStm.setFloat(index, value)
      case value: Double => jdbcStm.setDouble(index, value)
      case value: BigDecimal => jdbcStm.setBigDecimal(index, value.bigDecimal)
      case value: Time => jdbcStm.setTime(index, value)
      case value: Date => jdbcStm.setDate(index, value)
      case value: Timestamp => jdbcStm.setTimestamp(index, value)
      case value: Jsonb => jdbcStm.setObject(index, jsonbArg(value))
      case value: UUID => jdbcStm.setObject(index, value)
      case value: PGInterval => jdbcStm.setObject(index, value)
      case value: TypeNull => jdbcStm.setNull(index, value.typeId)
      case value: TypeArray => jdbcStm.setArray(index, arrayArg(value))
      case _ => throw SlinqInvalidArgException(s"type not supported [$arg]")
    }

  private def getStatement(sql: String, args: Vector[Any]) = {

    val jdbcStm = conn.prepareStatement(sql)

    if (args.nonEmpty) {
      args.filter(notNoArg).zipWithIndex.foreach { case (arg, index) =>
        setArg(jdbcStm, arg, index + 1)
      }
    }
    jdbcStm
  }

  def runQuery[R](stm: RenderedQuery[R]): List[R] = {
    val jdbcStm = getStatement(stm.statement, stm.args)
    val jdbcResultSet = jdbcStm.executeQuery()
    val buff = ListBuffer.empty[R]
    while (jdbcResultSet.next()) {
      try {
        buff += stm.rowConv.fromRow(jdbcResultSet)
      } catch {
        case ex: Throwable =>
          throw SlinqResultTypeException("our model may not match the table", ex)
      }
    }
    jdbcResultSet.close()
    jdbcStm.close()
    buff.toList
  }

  def runExec(stm: RenderedOperation): Unit = {
    val jdbcStm = getStatement(stm.statement, stm.args)
    jdbcStm.execute()
    jdbcStm.close()
  }

  def runExecNum(stm: RenderedOperation): Int = {
    val jdbcStm = getStatement(stm.statement, stm.args)
    val num = jdbcStm.executeUpdate()
    jdbcStm.close()
    num
  }

  def runExecList(stms: Seq[RenderedOperation]): Unit =
    try {
      conn.setAutoCommit(false)
      stms.foreach { stm =>
        val jdbcStm = getStatement(stm.statement, stm.args)
        jdbcStm.execute()
      }
      conn.commit()
      conn.setAutoCommit(true)
    } catch {
      case e: Throwable =>
        conn.rollback()
        conn.setAutoCommit(true)
        throw e
    }
}
