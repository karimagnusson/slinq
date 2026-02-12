package slinq.pg.update

import slinq.pg.shape.ParamConv
import slinq.pg.column.TypeCol
import slinq.pg.render.SectionCollector
import slinq.pg.section.ReturningSec
import slinq.pg.shape.*
import slinq.pg.fn.Fn
import slinq.pg.shape.TupleOfTypeCols


abstract class PickStoredUpdateReturning[M, P1, P2](
  model: M,
  coll: SectionCollector,
  changes: ParamConv[P1],
  filters: ParamConv[P2]
) {

  private def next[R](rowShape: RowShape[R]) = {
    new RenderStoredUpdateReturning(
      coll.add(ReturningSec(rowShape.cols)),
      changes: ParamConv[P1],
      filters: ParamConv[P2],
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
  def returning[T](pick: M => T)(using ev: TupleOfTypeCols[T]): RenderStoredUpdateReturning[P1, P2, ev.Out] = {
    next(ev.toRowShape(pick(model)))
  }
}
