#! /usr/bin/env python


func = """
  def returning%s[%s](
        pick: M => Tuple%s[%s]
      ) = {
    next(
      new RowShape%s(pick(model))
    )
  }"""


template = """package kuzminki.delete

import kuzminki.column.TypeCol
import kuzminki.render.SectionCollector
import kuzminki.section.ReturningSec
import kuzminki.shape._
import kuzminki.fn.Fn


abstract class PickStoredDeleteReturning[M, P](
  model: M,
  coll: SectionCollector,
  paramConv: ParamConv[P]
) { 

  private def next[R](rowShape: RowShape[R]) = {
    new RenderStoredDeleteReturning(
      coll.add(ReturningSec(rowShape.cols)),
      paramConv,
      rowShape.conv
    )
  }

  def returningType[R](pick: M => RowReader[R]) = {
    next(
      pick(model)
    )
  }

  def returningSeq(pick: M => Seq[TypeCol[_]]) = {
    next(
      new RowShapeSeq(pick(model))
    )
  }

  def returningNamed(pick: M => Seq[Tuple2[String, TypeCol[_]]]) = {
    next(
      new RowShapeNamed(pick(model))
    )
  }

  def returningJson(pick: M => Seq[Tuple2[String, TypeCol[_]]]) = {
    next(
      new RowShapeSingle(
        Fn.json(pick(model))
      )
    )
  }

  def returning1[R](pick: M => TypeCol[R]) = {
    next(
      new RowShapeSingle(pick(model))
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

f = open('../scala/delete/PickStoredDeleteReturning.scala', 'w')
f.write(content)
f.close()