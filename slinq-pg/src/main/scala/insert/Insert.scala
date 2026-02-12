package slinq.pg.insert

import slinq.pg.api.Model
import slinq.pg.column.TypeCol
import slinq.pg.assign.SetValue
import slinq.pg.shape.*


class Insert[M <: Model](model: M) {

  def data(pick: M => Seq[SetValue]) = {
    val pairs = pick(model).toVector
    new Values(
      ValuesBuilder(
        model,
        pairs.map(_.col),
        pairs.map(_.arg)
      )
    )
  }

  private def next[P](paramShape: ParamShape[P]) = {
    new InsertOptions(
      InsertBuilder(
        model,
        paramShape
      )
    )
  }

  // Single polymorphic cols method for TypeCol
  def cols[P](pick: M => TypeCol[P]) = {
    next(new ParamShapeSingle(pick(model)))
  }

  // Single polymorphic cols method for tuples
  def cols[T <: Tuple](pick: M => T)(using ev: TupleOfTypeCols[T]): InsertOptions[M, ev.Out] = {
    next(new ParamShapeTupled(pick(model)).asInstanceOf[ParamShape[ev.Out]])
  }
}