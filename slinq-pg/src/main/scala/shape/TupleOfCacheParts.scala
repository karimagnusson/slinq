package slinq.pg.shape


// Type class for tuples of CachePart (which includes CacheFilter)
trait TupleOfCacheParts[T] {
  type Out
  type IsSingle <: Boolean
  def toPartShape(t: T): PartShape[Out]
}

object TupleOfCacheParts {

  type Aux[T, O, S <: Boolean] = TupleOfCacheParts[T] { type Out = O; type IsSingle = S }

  // Single CachePart - accepts any subtype of CachePart[P]
  given single[C <: CachePart[P], P]: Aux[C, P, true] = new TupleOfCacheParts[C] {
    type Out = P
    type IsSingle = true
    def toPartShape(t: C) = new PartShapeSingle(t)
  }

  // Any tuple of CachePart - uses the polymorphic PartShapeTupled
  given tupled[T <: Tuple]: Aux[T, ExtractPartParamTypes[T], false] = new TupleOfCacheParts[T] {
    type Out = ExtractPartParamTypes[T]
    type IsSingle = false
    def toPartShape(t: T) = new PartShapeTupled(t)
  }
}
