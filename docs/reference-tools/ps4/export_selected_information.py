"""Read the selected-label typography and attribute filter from pinned PS4 inputs."""
import argparse
import hashlib
import json
import struct
from pathlib import Path
from read_fui import Fui


def export(hud, labels, executable, information, evidence):
    assert hashlib.sha256(executable.read_bytes()).hexdigest() == '1bc106605c15cd23a02bc28c03c36060bf5e4f6a185e709e28c7e5ba45eb3cdb'
    assert hashlib.sha256(labels.read_bytes()).hexdigest() == '7ae77ad6df090546a50be3cb8b61f500c361d4e93a7c20f59eedab9ee7309351'
    h, g = Fui(hud), Fui(labels)
    selected = next(n for n in h.geometry if n['name'] == 'SelectedLabel')
    frame = g.frames[g.defs[g.exports['FJ_Label_White_Centered'][1]][1]]
    field = g.geometry[frame['start']]
    label = dict(g.textfields[field['header'][3]], matrix=field['matrix'])
    data = json.loads(information.read_text())
    assert hashlib.sha256(hud.read_bytes()).hexdigest() == data['files']['HUD1080.fui']
    data['selected_label'] = label
    information.write_text(json.dumps(data, indent=2) + '\n')
    raw = executable.read_bytes()
    header = struct.unpack_from('<16sHHIQQQIHHHHHH', raw)
    segments = [struct.unpack_from('<IIQQQQQQ', raw, header[5]+i*header[9])
                for i in range(header[10])]
    def read(address, size):
        segment = next(s for s in segments if s[0] in (1, 0x61000010)
                       and s[3] <= address and address+size <= s[3]+s[5])
        offset = segment[2]+address-segment[3]
        return raw[offset:offset+size]
    def wide(address):
        return read(address, 160).decode('utf-16le').split('\0')[0]
    names = ['max_health', 'follow_range', 'knockback_resistance', 'movement_speed',
             'attack_damage', 'horse_jump_strength', 'zombie_reinforcements',
             'attack_speed', 'armor', 'armor_toughness', 'luck']
    fixture = dict(repository=data['repository'], revision=data['revision'],
        inputs={p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in (hud, labels, executable)},
        routines=dict(tooltip='0x3bb630', attribute_format='0x3cda0',
                      selected_label='0xa4e3f0 / 0x813030', line_limit='0xc78a90',
                      label_constructor='0x7dabf0', label_set_text='0x7dae40',
                      control_set_text='0x7d1170'),
        selected_matrix=selected['matrix'], selected_label=label,
        number_format=wide(0xe71eee), percentage_format=wide(0xe71ece),
        attributes=[dict(id=i, name=name,
                         label_crc=hex(struct.unpack('<I', read(0xe72a90+i*4, 4))[0]),
                         supported=i < 7 or i == 8) for i, name in enumerate(names)])
    label_vtable = 0x1300928
    setter = struct.unpack('<Q', read(label_vtable+0x50, 8))[0]
    assert setter == 0x7dae40
    fixture['label_set_text'] = dict(vtable=hex(label_vtable), virtual_offset='0x50',
        target=hex(setter), code_sha256=hashlib.sha256(read(setter,0x40)).hexdigest(),
        constructor_sha256=hashlib.sha256(read(0x7dabf0,0x1d0)).hexdigest(),
        control_setter_sha256=hashlib.sha256(read(0x7d1170,0x780)).hexdigest())
    evidence.write_text(json.dumps(fixture, indent=2) + '\n')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    for name in ('hud', 'labels', 'executable', 'information', 'evidence'):
        parser.add_argument('--'+name, type=Path, required=True)
    args = parser.parse_args()
    export(args.hud, args.labels, args.executable, args.information, args.evidence)
