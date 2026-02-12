package slinq.pg.delete

import slinq.pg.column.TypeCol
import slinq.pg.render.SectionCollector
import slinq.pg.section.ReturningSec
import slinq.pg.shape.*
import slinq.pg.fn.Fn


abstract class PickDeleteReturning[M](model: M, coll: SectionCollector) {

  private def next[R](rowShape: RowShape[R]) = {
    new RenderDeleteReturning(
      coll.add(ReturningSec(rowShape.cols)),
      rowShape.conv
    )
  }

  def returningType[R](pick: M => RowReader[R]) = {
    next(
      pick(model)
    )
  }

  def returningSeq(pick: M => Seq[TypeCol[?]]) = {
    next(
      new RowShapeSeq(pick(model))
    )
  }

  def returningNamed(pick: M => Seq[Tuple2[String, TypeCol[?]]]) = {
    next(
      new RowShapeNamed(pick(model))
    )
  }

  def returningJson(pick: M => Seq[Tuple2[String, TypeCol[?]]]) = {
    next(
      new RowShapeSingle(
        Fn.json(pick(model))
      )
    )
  }

  // Polymorphic returning method that works with any tuple size
  def returning[T](pick: M => T)(using ev: TupleOfTypeCols[T]): RenderDeleteReturning[ev.Out] = {
    next(ev.toRowShape(pick(model)))
  }
}