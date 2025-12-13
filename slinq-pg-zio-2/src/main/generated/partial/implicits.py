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

filter_types = [
    ('String', 'StringMethods'),
    ('Boolean', 'BooleanMethods'),
    ('Short', 'NumericMethods[Short]'),
    ('Int', 'NumericMethods[Int]'),
    ('Long', 'NumericMethods[Long]'),
    ('Float', 'NumericMethods[Float]'),
    ('Double', 'NumericMethods[Double]'),
    ('BigDecimal', 'NumericMethods[BigDecimal]'),
    ('Time', 'TimeMethods'),
    ('Date', 'DateMethods'),
    ('Timestamp', 'TimestampMethods'),
    ('Jsonb', 'JsonbMethods'),
    ('UUID', 'TypeMethods[UUID]')
]

impl_ref_1 = '  implicit val kz%sCol: ColInfo => TypeCol[%s] = info => %sModelCol(info)\n'
impl_seq_1 = '  implicit val kz%sSeqCol: ColInfo => TypeCol[Seq[%s]] = info => %sSeqModelCol(info)\n'

impl_ref_2 = '  implicit class Kz%sImpl(val col: TypeCol[%s]) extends %s\n'
impl_seq_2 = '  implicit class Kz%sSeqImpl(val col: TypeCol[Seq[%s]]) extends SeqMethods[%s]\n'

parts = []

parts.extend(['\n', '  // create model col\n', '\n'])

for name in java_ref_types:
    parts.append(impl_ref_1 % (name, name, name))

parts.append('\n')

for name in java_ref_types:
    if name not in ('UUID',):
        parts.append(impl_seq_1 % (name, name, name))

parts.extend(['\n', '  // filters\n', '\n'])

for name, filters in filter_types:
    parts.append(impl_ref_2 % (name, name, filters))

parts.append('\n')

for name in java_ref_types:
    if name not in ('UUID',):
        parts.append(impl_seq_2 % (name, name, name))

parts.append('\n')

content = ''.join(parts)

f = open('./output/implicits.txt', 'w')
f.write(content)
f.close()









