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

impl_ref_1 = '  given kz%sCol: Conversion[ColInfo, TypeCol[%s]] = (x: ColInfo) => %sModelCol(x)\n'
impl_seq_1 = '  given kz%sSeqCol: Conversion[ColInfo, TypeCol[Seq[%s]]] = (x: ColInfo) => %sSeqModelCol(x)\n'






parts = []

parts.extend(['\n', '  // create model col\n', '\n'])

for name in java_ref_types:
    parts.append(impl_ref_1 % (name, name, name))

parts.append('\n')

for name in java_ref_types:
    if name not in ('UUID',):
        parts.append(impl_seq_1 % (name, name, name))

parts.extend(['\n', '  // filters\n', '\n'])

parts.append('\n')

content = ''.join(parts)

f = open('./output/implicits-3.txt', 'w')
f.write(content)
f.close()









