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
    'Timestamp'
]


tmpl = '    case %sSeqConv => %sConv'
tmpl_seq = '    case %sConv => %sSeqConv'

parts = []


for name in java_ref_types:
    parts.append(tmpl % (name, name))
    parts.append('\n')

parts.append('\n')

for name in java_ref_types:
    parts.append(tmpl_seq % (name, name))
    parts.append('\n')

content = ''.join(parts)

f = open('./output/array_conv.txt', 'w')
f.write(content)
f.close()









