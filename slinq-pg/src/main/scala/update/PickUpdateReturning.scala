package slinq.pg.update

import slinq.pg.column.TypeCol
import slinq.pg.render.SectionCollector
import slinq.pg.section.ReturningSec
import slinq.pg.shape.*
import slinq.pg.fn.Fn
import slinq.pg.shape.TupleOfTypeCols


abstract class PickUpdateReturning[M](model: M, coll: SectionCollector) {

  def next[R](rowShape: RowShape[R]) = {
    new RenderUpdateReturning(
      coll.add(ReturningSec(rowShape.cols)),
      rowShape.conv
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
  def returning[T](pick: M => T)(using ev: TupleOfTypeCols[T]): RenderUpdateReturning[ev.Out] = {
    next(ev.toRowShape(pick(model)))
  }
}
