#! /usr/bin/env python


func = """
  def pickWhere%s[%s](
    pick: M => Tuple%s[%s]
  ) = {
    next(new PartShape%s(pick(model)))
  }"""


template = """package kuzminki.delete

import kuzminki.render.SectionCollector
import kuzminki.section.operation.UpdateCacheWhereSec
import kuzminki.filter.types.CacheFilter
import kuzminki.shape._


abstract class PickDeleteWhere[M](model: M, coll: SectionCollector) {

  private def next[A](partShape: PartShape[A]) = {
    new RenderStoredDelete(
      model,
      coll.add(UpdateCacheWhereSec(partShape.parts)),
      partShape.conv
    )
  }

  def pickWhere1[P](
    pick: M => CacheFilter[P]
  ) = {
    next(new PartShapeSingle(pick(model)))
  }
%s
}
"""


parts = []

for num in range(2, 23):
    types = ', '.join(['P%d' % i for i in range(1, num + 1)])
    col_types = ', '.join(['CacheFilter[P%d]' % i for i in range(1, num + 1)])
    conds = ', '.join(['cond%d' % i for i in range(1, num + 1)])
    conv = ', '.join(['cond%d.conv' % i for i in range(1, num + 1)])
    
    part = func % (
      str(num),
      types,
      str(num),
      col_types,
      str(num),
    )
    
    parts.append(part)

content = template % "\n".join(parts)

f = open('../scala/delete/PickDeleteWhere.scala', 'w')
f.write(content)
f.close()












