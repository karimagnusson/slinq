#! /usr/bin/env python


types = [
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
import kuzminki.conv._
import kuzminki.filter._
import kuzminki.api.Jsonb


'''

template = '''trait %sCol extends TypeCol[%s] {
  val conv = %sConv
}

'''

template_seq = '''trait %sCol extends TypeCol[Seq[%s]] {
  val conv = %sConv
}

'''


parts = [imports]

for name in types:
    parts.append(template % (name, name, name))

for name in types:
    if name not in ('UUID',):
        parts.append(template_seq % (name + 'Seq', name, name + 'Seq'))

content = ''.join(parts)

f = open('../scala/column/ColTypes.scala', 'w')
f.write(content)
f.close()









