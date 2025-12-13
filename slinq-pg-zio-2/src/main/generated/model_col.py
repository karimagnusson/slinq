#! /usr/bin/env python


java_ref_types = [
    'String',
    'Boolean',
    'Short',
    'Int',
    'Long',
    'Float',
    'Double',
    'BigDecimal',
    'Time',
    'Date',
    'Timestamp',
    'Jsonb',
    'UUID'
]



imports = '''package kuzminki.column

import java.sql.Time
import java.sql.Date
import java.sql.Timestamp
import java.util.UUID
import kuzminki.filter._
import kuzminki.render.{Prefix, NoArgs}


trait ModelCol extends NoArgs {          
  val info: ColInfo
  def name = info.name
  def render(prefix: Prefix) = prefix.pick(info)
}

'''

template = 'case class %sModelCol(info: ColInfo) extends %sCol with ModelCol\n\n'

template_seq = 'case class %sSeqModelCol(info: ColInfo) extends %sSeqCol with ModelCol\n\n'



parts = [imports]

for name in java_ref_types:
    parts.append(template % (name, name))

for name in java_ref_types:
    if name not in ('Jsonb', 'UUID',):
        parts.append(template_seq % (name, name))

content = ''.join(parts)

f = open('../scala/column/ModelCol.scala', 'w')

f.write(content)
f.close()









