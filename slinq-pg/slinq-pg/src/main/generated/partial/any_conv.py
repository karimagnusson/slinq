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

base_types = java_ref_types + ['Jsonb', 'UUID']


tmpl = '    case %sConv => %sConv.put(value.asInstanceOf[%s])'
tmpl_opt = '    case %sOptConv => %sOptConv.put(value.asInstanceOf[Option[%s]])'
tmpl_seq = '    case %sSeqConv => %sSeqConv.put(value.asInstanceOf[Seq[%s]])'
tmpl_seq_opt = '    case %sSeqOptConv => %sSeqOptConv.put(value.asInstanceOf[Option[Seq[%s]]])'

parts = []


for name in base_types:
    parts.append(tmpl % (name, name, name))
    parts.append('\n')

for name in base_types:
    parts.append(tmpl_opt % (name, name, name))
    parts.append('\n')

for name in java_ref_types:
    parts.append(tmpl_seq % (name, name, name))
    parts.append('\n')

for name in java_ref_types:
    parts.append(tmpl_seq_opt % (name, name, name))
    parts.append('\n')

content = ''.join(parts)

f = open('./output/any_conv.txt', 'w')
f.write(content)
f.close()









