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
    'UUID',
    'StringSeq',
    'BooleanSeq',
    'ShortSeq',
    'IntSeq',
    'LongSeq',
    'FloatSeq',
    'DoubleSeq',
    'BigDecimalSeq',
    'TimeSeq',
    'DateSeq',
    'TimestampSeq'
]

fn_tmpl_1 = 'trait %sFn extends %sCol with CustomFn'
fn_tmpl_2 = 'trait %sParamsFn extends %sCol with CustomParamsFn'
fn_tmpl_3 = '@deprecated("this trait will be removed", "0.9.5")\ntrait %sNoArgsFn extends %sCol with FnColArgs\n'
fn_tmpl_4 = '@deprecated("this trait will be removed", "0.9.5")\ntrait %sArgsFn extends %sCol with FnArgs\n'

parts = []

for name in java_ref_types:
    parts.append(fn_tmpl_1 % (name, name))
    parts.append('\n')

parts.append('\n')

for name in java_ref_types:
    parts.append(fn_tmpl_2 % (name, name))
    parts.append('\n')

parts.append('\n')

for name in java_ref_types:
    parts.append(fn_tmpl_3 % (name, name))
    parts.append(fn_tmpl_4 % (name, name))
    parts.append('\n')

content = ''.join(parts)

f = open('./output/fn.txt', 'w')
f.write(content)
f.close()









