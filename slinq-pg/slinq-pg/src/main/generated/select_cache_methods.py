#! /usr/bin/env python


func = """
  def pickWhere%s[%s](
    pick: M => Tuple%s[%s]
  ) = new SelectCacheMultiple(coll, new PartShape%s(pick(model)))"""


template = """package kuzminki.select

import kuzminki.shape._
import kuzminki.filter.types.CacheFilter


abstract class SelectCacheMethods[M, R](model: M, coll: SelectCollector[R]) {

  def pickWhere1[P](pick: M => CacheFilter[P]) =
    new SelectCacheSingle(coll, new PartShapeSingle(pick(model)))
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

f = open('../scala/select/SelectCacheMethods.scala', 'w')
f.write(content)
f.close()




















