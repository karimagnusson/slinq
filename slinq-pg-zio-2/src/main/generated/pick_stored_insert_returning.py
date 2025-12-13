#! /usr/bin/env python


func = """
  def returning%s[%s](
        pick: M => Tuple%s[%s]
      ) = {
    next(
      new RowShape%s(pick(builder.model))
    )
  }"""



template = """package kuzminki.insert

import kuzminki.api.Model
import kuzminki.column.TypeCol
import kuzminki.render.SectionCollector
import kuzminki.shape._
import kuzminki.fn.Fn


abstract class PickStoredInsertReturning[M <: Model, P](builder: InsertBuilder[M, P]) {

  private def next[R](rowShape: RowShape[R]) = {
    new RenderStoredInsertReturning(
      builder.returning(rowShape.cols),
      builder.paramShape.conv,
      rowShape.conv
    )
  }

  def returningType[R](pick: M => RowReader[R]) = {
    next(
      pick(builder.model)
    )
  }

  def returningSeq(pick: M => Seq[TypeCol[_]]) = {
    next(
      new RowShapeSeq(pick(builder.model))
    )
  }

  def returningNamed(pick: M => Seq[Tuple2[String, TypeCol[_]]]) = {
    next(
      new RowShapeNamed(pick(builder.model))
    )
  }

  def returningJson(pick: M => Seq[Tuple2[String, TypeCol[_]]]) = {
    next(
      new RowShapeSingle(
        Fn.json(pick(builder.model))
      )
    )
  }

  def returning1[R](pick: M => TypeCol[R]) = {
    next(
      new RowShapeSingle(pick(builder.model))
    )
  }
  %s
}"""


parts = []

for num in range(2, 23):
    func_types = ', '.join(['R%d' % i for i in range(1, num + 1)])
    col_types = ', '.join(['TypeCol[R%d]' % i for i in range(1, num + 1)])
    part = func % (str(num), func_types, str(num), col_types, str(num),)
    parts.append(part)

content = template % "\n".join(parts)

f = open('../scala/insert/PickStoredInsertReturning.scala', 'w')
f.write(content)
f.close()