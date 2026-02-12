package slinq.pg.update

import slinq.pg.api.Model
import slinq.pg.model.ModelTable
import slinq.pg.render.{Prefix, SectionCollector}
import slinq.pg.section.{UpdateSec, UpdateCacheSetSec}
import slinq.pg.shape.*


abstract class PickUpdateSet[M <: Model](model: M) {

  private def next[S1](changes: PartShape[S1]) = {
    new PickUpdateWhere(
      model,
      SectionCollector(
        Prefix.forModel(model),
        Vector(
          UpdateSec(ModelTable(model)),
          UpdateCacheSetSec(changes.parts)
        )
      ),
      changes
    )
  }

  // Single polymorphic pickSet method
  def pickSet[T](pick: M => T)(using ev: TupleOfCacheParts[T]): PickUpdateWhere[M, ev.Out] = {
    next(ev.toPartShape(pick(model)).asInstanceOf[PartShape[ev.Out]])
  }
}
