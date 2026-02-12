package slinq.pg.insert

import slinq.pg.api.Model
import slinq.pg.column.TypeCol
import slinq.pg.shape.*
import slinq.pg.fn.Fn
import slinq.pg.shape.TupleOfTypeCols


abstract class PickStoredInsertReturning[M <: Model, P](builder: InsertBuilder[M, P]) {

  private def next[R](rowShape: RowShape[R]) = {
    new RenderStoredInsertReturning(
      builder.returning(rowShape.cols),
      builder.paramShape.conv,
      rowShape.conv
    )
  }

  def returningSeq(pick: M => Seq[TypeCol[?]]) = {
    next(
      new RowShapeSeq(pick(builder.model))
    )
  }

  def returningNamed(pick: M => Seq[Tuple2[String, TypeCol[?]]]) = {
    next(
      new RowShapeNamed(pick(builder.model))
    )
  }

  def returningJson(pick: M => Seq[Tuple2[String, TypeCol[?]]]) = {
    next(
      new RowShapeSingle(
        Fn.json(pick(builder.model))
      )
    )
  }

  // Polymorphic returning method that works with any tuple size
  def returning[T](pick: M => T)(using ev: TupleOfTypeCols[T]): RenderStoredInsertReturning[P, ev.Out] = {
    next(ev.toRowShape(pick(builder.model)))
  }
}
