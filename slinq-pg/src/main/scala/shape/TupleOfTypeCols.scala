package slinq.pg.shape

import slinq.pg.column.TypeCol


// Helper trait to extract types from tuples of TypeCol
trait TupleOfTypeCols[T] {
  type Out
  def toRowShape(t: T): RowShape[Out]
}

object TupleOfTypeCols {

  type Aux[T, O] = TupleOfTypeCols[T] { type Out = O }

  // Single column - accepts any subtype of TypeCol[R]
  given single[C <: TypeCol[R], R]: Aux[C, R] = new TupleOfTypeCols[C] {
    type Out = R
    def toRowShape(t: C) = new RowShapeSingle(t)
  }

  // Any tuple of TypeCol - uses the polymorphic RowShapeTupled
  given tupled[T <: Tuple]: Aux[T, ExtractColTypes[T]] = new TupleOfTypeCols[T] {
    type Out = ExtractColTypes[T]
    def toRowShape(t: T) = new RowShapeTupled(t)
  }
}
