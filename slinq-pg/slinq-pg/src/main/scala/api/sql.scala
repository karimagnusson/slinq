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

package slinq.pg.api

import slinq.pg.select.{
  Select,
  SelectJoin,
  Where,
  JoinOn
}
import slinq.pg.delete.{Delete, DeleteWhere}
import slinq.pg.insert.Insert
import slinq.pg.update.Update
import slinq.pg.fn.Count
import slinq.pg.render.{Transaction, RenderedOperation}


trait SqlApi {

  def select[M <: Model](model: M): Select[M] = {
    new Select(model)
  }

  def select[A <: Model, B <: Model](a: A, b: B): SelectJoin[A, B] = {
    new SelectJoin(Join(a, b))
  }

  def count[M <: Model](model: M): Where[M, Long] = {
    new Select(model).cols1(t => Count.all)
  }

  def count[A <: Model, B <: Model](a: A, b: B): JoinOn[A, B, Long] = {
    new SelectJoin(Join(a, b)).cols1(t => Count.all)
  }

  def insert[M <: Model](model: M): Insert[M] = {
    new Insert(model)
  }

  def update[M <: Model](model: M): Update[M] = {
    new Update(model)
  }

  def delete[M <: Model](model: M): DeleteWhere[M] = {
    Delete.from(model)
  }

  def transaction(stms: RenderedOperation*) = new Transaction(stms)

  def transactionList(stms: Seq[RenderedOperation]) = new Transaction(stms)
}

object sql extends SqlApi

object pgsql extends SqlApi






















