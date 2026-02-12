package slinq.pg.update

import slinq.pg.render.SectionCollector
import slinq.pg.section.UpdateCacheWhereSec
import slinq.pg.shape.*


class PickUpdateWhere[M, A](
  model: M,
  coll: SectionCollector,
  changes: PartShape[A]
) {

  private def next[B](filters: PartShape[B]) = {
    new RenderStoredUpdate(
      model,
      coll.add(UpdateCacheWhereSec(filters.parts)),
      changes.conv,
      filters.conv
    )
  }

  // Single polymorphic pickWhere method
  def pickWhere[T](pick: M => T)(using ev: TupleOfCacheParts[T]): RenderStoredUpdate[M, A, ev.Out] = {
    next(ev.toPartShape(pick(model)).asInstanceOf[PartShape[ev.Out]])
  }
}
