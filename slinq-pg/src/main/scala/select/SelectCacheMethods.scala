package slinq.pg.select

import slinq.pg.shape.*
import slinq.pg.filter.types.CacheFilter


abstract class SelectCacheMethods[M, R](model: M, coll: SelectCollector[R]) {

  // Overloaded pickWhere for single CacheFilter
  def pickWhere[P](pick: M => CacheFilter[P]): SelectCacheSingle[P, R] =
    new SelectCacheSingle(coll, new PartShapeSingle(pick(model)))

  // Overloaded pickWhere for tuples
  def pickWhere[T <: Tuple](pick: M => T)(using ev: TupleOfCacheParts[T]): SelectCacheMultiple[ev.Out, R] =
    new SelectCacheMultiple(coll, ev.toPartShape(pick(model)).asInstanceOf[PartShape[ev.Out]])
}
