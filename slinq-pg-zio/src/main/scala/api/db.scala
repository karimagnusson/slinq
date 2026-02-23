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

package slinq.pg.zio.api

import java.sql.SQLException
import slinq.pg.render.{RenderedQuery, RenderedOperation}

import _root_.zio.*

object db {

  def query[R](render: => RenderedQuery[R]): ZIO[SlinqPg, SQLException, List[R]] =
    for {
      inst <- SlinqPg.get
      rows <- inst.query(render).refineToOrDie[SQLException]
    } yield rows

  def queryAs[R, T](
    render: => RenderedQuery[R],
    transform: R => T): ZIO[SlinqPg, SQLException, List[T]] =
    for {
      inst <- SlinqPg.get
      rows <- inst.queryAs(render, transform).refineToOrDie[SQLException]
    } yield rows

  def queryHead[R](render: => RenderedQuery[R]): ZIO[SlinqPg, SQLException, R] =
    for {
      inst <- SlinqPg.get
      head <- inst.queryHead(render).refineToOrDie[SQLException]
    } yield head

  def queryHeadAs[R, T](
    render: => RenderedQuery[R],
    transform: R => T): ZIO[SlinqPg, SQLException, T] =
    for {
      inst <- SlinqPg.get
      rows <- inst.queryHeadAs(render, transform).refineToOrDie[SQLException]
    } yield rows

  def queryHeadOpt[R](render: => RenderedQuery[R]): ZIO[SlinqPg, SQLException, Option[R]] =
    for {
      inst    <- SlinqPg.get
      headOpt <- inst.queryHeadOpt(render).refineToOrDie[SQLException]
    } yield headOpt

  def queryHeadOptAs[R, T](
    render: => RenderedQuery[R],
    transform: R => T): ZIO[SlinqPg, SQLException, Option[T]] =
    for {
      inst    <- SlinqPg.get
      headOpt <- inst.queryHeadOptAs(render, transform).refineToOrDie[SQLException]
    } yield headOpt

  def exec(render: => RenderedOperation): ZIO[SlinqPg, SQLException, Unit] =
    for {
      inst <- SlinqPg.get
      _    <- inst.exec(render).refineToOrDie[SQLException]
    } yield ()

  def execNum(render: => RenderedOperation): ZIO[SlinqPg, SQLException, Int] =
    for {
      inst <- SlinqPg.get
      num  <- inst.execNum(render).refineToOrDie[SQLException]
    } yield num

  def execList(stms: Seq[RenderedOperation]): ZIO[SlinqPg, SQLException, Unit] =
    for {
      inst <- SlinqPg.get
      _    <- inst.execList(stms).refineToOrDie[SQLException]
    } yield ()

}
