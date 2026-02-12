package slinq.pg.select

import slinq.pg.api.Model
import slinq.pg.model.ModelTable
import slinq.pg.column.TypeCol
import slinq.pg.render.Prefix
import slinq.pg.section.*
import slinq.pg.shape.*
import slinq.pg.fn.Fn


class Select[M <: Model](val model: M) {

  def next[R](rowShape: RowShape[R]) = {
    new Where(
      model,
      SelectCollector(
        Prefix.forModel(model),
        rowShape,
        Vector(
          SelectSec(rowShape.cols),
          FromSec(ModelTable(model))
        )
      )
    )
  }

  def colsSeq(pick: M => Seq[TypeCol[?]]) = {
    next(
      new RowShapeSeq(pick(model))
    )
  }

  def colsNamed(pick: M => Seq[Tuple2[String, TypeCol[?]]]) = {
    next(
      new RowShapeNamed(pick(model))
    )
  }

  def colsJson(pick: M => Seq[Tuple2[String, TypeCol[?]]]) = {
    next(
      new RowShapeSingle(
        Fn.json(pick(model))
      )
    )
  }

  // Polymorphic cols method that works with any tuple size
  def cols[T](pick: M => T)(using ev: TupleOfTypeCols[T]): Where[M, ev.Out] = {
    next(ev.toRowShape(pick(model)))
  }

}
