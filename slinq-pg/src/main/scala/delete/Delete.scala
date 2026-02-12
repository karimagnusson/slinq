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

package slinq.pg.delete

import slinq.pg.api.Model
import slinq.pg.model.ModelTable
import slinq.pg.render.{Prefix, SectionCollector}
import slinq.pg.section.DeleteFromSec


object Delete {

  def from[M <: Model](model: M) = {
    new DeleteWhere(
      model,
      SectionCollector(
        Prefix.forModel(model),
        Vector(
          DeleteFromSec(ModelTable(model))
        )
      )
    )
  }
}