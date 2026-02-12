package slinq.pg.select

import slinq.pg.api.{Model, Join}
import slinq.pg.model.ModelTable
import slinq.pg.column.TypeCol
import slinq.pg.render.Prefix
import slinq.pg.section.*
import slinq.pg.shape.*
import slinq.pg.fn.Fn


class SelectJoin[A <: Model, B <: Model](val join: Join[A, B]) {

  def next[R](outShape: RowShape[R]) = {
    new JoinOn(
      join,
      SelectCollector(
        Prefix.forJoin(join),
        outShape,
        Vector(
          SelectSec(outShape.cols),
          FromSec(ModelTable(join.a))
        )
      )
    )
  }

  def colsSeq(pick: Join[A, B] => Seq[TypeCol[?]]) = {
    next(
      new RowShapeSeq(pick(join))
    )
  }

  def colsNamed(pick: Join[A, B] => Seq[Tuple2[String, TypeCol[?]]]) = {
    next(
      new RowShapeNamed(pick(join))
    )
  }

  def colsJson(pick: Join[A, B] => Seq[Tuple2[String, TypeCol[?]]]) = {
    next(
      new RowShapeSingle(
        Fn.json(pick(join))
      )
    )
  }

  // Polymorphic cols method that works with any tuple size
  def cols[T](pick: Join[A, B] => T)(using ev: TupleOfTypeCols[T]): JoinOn[A, B, ev.Out] = {
    next(ev.toRowShape(pick(join)))
  }
}