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

package slinq.pg.select

import slinq.pg.api.{Model, Join}
import slinq.pg.model.ModelTable
import slinq.pg.column.TypeCol
import slinq.pg.section.*


class JoinOn[A <: Model, B <: Model, R](join: Join[A, B], coll: SelectCollector[R]) {

  private def next(joinSec: JoinSec, onSec: OnSec) = {
    new Where(join, coll.extend(Vector(joinSec, onSec)))
  }

  def joinOn(pickLeft: A => TypeCol[?], pickRight: B => TypeCol[?]) = {
    next(
      InnerJoinSec(ModelTable(join.b)),
      OnSec(pickLeft(join.a), pickRight(join.b))
    )
  }

  def innerJoinOn(pickLeft: A => TypeCol[?], pickRight: B => TypeCol[?]) = {
    next(
      InnerJoinSec(ModelTable(join.b)),
      OnSec(pickLeft(join.a), pickRight(join.b))
    )
  }

  def leftJoinOn(pickLeft: A => TypeCol[?], pickRight: B => TypeCol[?]) = {
    next(
      LeftJoinSec(ModelTable(join.b)),
      OnSec(pickLeft(join.a), pickRight(join.b))
    )
  }

  def leftOuterJoinOn(pickLeft: A => TypeCol[?], pickRight: B => TypeCol[?]) = {
    next(
      LeftOuterJoinSec(ModelTable(join.b)),
      OnSec(pickLeft(join.a), pickRight(join.b))
    )
  }

  def rightJoinOn(pickLeft: A => TypeCol[?], pickRight: B => TypeCol[?]) = {
    next(
      RightJoinSec(ModelTable(join.b)),
      OnSec(pickLeft(join.a), pickRight(join.b))
    )
  }

  def rightOuterJoinOn(pickLeft: A => TypeCol[?], pickRight: B => TypeCol[?]) = {
    next(
      RightOuterJoinSec(ModelTable(join.b)),
      OnSec(pickLeft(join.a), pickRight(join.b))
    )
  }

  def fullOuterJoinOn(pickLeft: A => TypeCol[?], pickRight: B => TypeCol[?]) = {
    next(
      FullOuterJoinSec(ModelTable(join.b)),
      OnSec(pickLeft(join.a), pickRight(join.b))
    )
  }

  def crossJoin = {
    new Where(join, coll.add(CrossJoinSec(ModelTable(join.b))))
  }
}













