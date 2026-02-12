package slinq.pg.delete

import slinq.pg.render.SectionCollector
import slinq.pg.section.UpdateCacheWhereSec
import slinq.pg.shape.*


abstract class PickDeleteWhere[M](model: M, coll: SectionCollector) {

  private def next[A](partShape: PartShape[A]) = {
    new RenderStoredDelete(
      model,
      coll.add(UpdateCacheWhereSec(partShape.parts)),
      partShape.conv
    )
  }

  // Single polymorphic pickWhere method
  def pickWhere[T](pick: M => T)(using ev: TupleOfCacheParts[T]): RenderStoredDelete[M, ev.Out] = {
    next(ev.toPartShape(pick(model)).asInstanceOf[PartShape[ev.Out]])
  }
}
