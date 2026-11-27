"""Execute original cursor/trading panel sizing with isolated text/UI callbacks.

The input glyph extent represents character zero, as requested by 0x863ea0;
it is independent of the multiline text field's full height.
"""
import argparse
import ctypes as C
import hashlib
import json
from pathlib import Path
import struct
import subprocess
import tempfile
from native_module import Module, P, I, F


def capture(eboot):
    with tempfile.TemporaryDirectory() as temporary:
        source = Path(temporary) / 'bounds.c'
        library = Path(temporary) / 'bounds.so'
        source.write_text('float glyph_height; struct rect {float x0,x1,y0,y1;};\n'
                          'struct rect bounds(void *p,int character) {'
                          'return (struct rect){0,0,0,glyph_height};}\n')
        subprocess.run(['cc', '-shared', '-fPIC', '-o', str(library), str(source)], check=True)
        dll = C.CDLL(str(library))
        glyph = C.c_float.in_dll(dll, 'glyph_height')
        module = Module(eboot)
        buffers, values = [], {}

        def allocate(size):
            buffer = C.create_string_buffer(size)
            buffers.append(buffer)
            return C.addressof(buffer)

        def write(pointer, offset, fmt, value):
            C.memmove(pointer + offset, struct.pack('<' + fmt, value), struct.calcsize('<' + fmt))

        def callback(result, args, function):
            cb = C.CFUNCTYPE(result, *args)(function)
            module.callbacks.append(cb)
            return cb

        def patch(address, result, args, function):
            module.patch(address, callback(result, args, function))

        def node():
            pointer, vtable = allocate(0x600), allocate(0x100)
            values[pointer] = dict(x=0., y=0., w=0., h=0.)
            write(pointer, 0, 'Q', vtable)
            for offset, key in [(0x50, 'x'), (0x58, 'y'), (0x60, 'w'), (0x68, 'h')]:
                cb = callback(None, [P, F], lambda p, value, key=key: values[p].__setitem__(key, value))
                write(vtable, offset, 'Q', C.cast(cb, P).value)
            for offset, key in [(0x70, 'w'), (0x78, 'h')]:
                cb = callback(F, [P], lambda p, key=key: values[p][key])
                write(vtable, offset, 'Q', C.cast(cb, P).value)
            return pointer, vtable

        text, _ = node()
        panel, _ = node()
        child, _ = node()
        cursor, _ = node()
        panel_wrapper, panel_vtable = node()
        child_wrapper, child_vtable = node()
        cursor_wrapper, _ = node()
        write(panel_wrapper, 8, 'Q', panel)
        write(cursor_wrapper, 8, 'Q', cursor)
        write(child_wrapper, 8, 'Q', child)
        children = allocate(8)
        write(children, 0, 'Q', child)
        write(panel, 0x108, 'Q', children)
        write(panel, 0x20, 'i', 2)
        write(child, 0xf8, 'Q', child_wrapper)
        values[cursor]['w'] = 48.
        for offset, key in [(0x18, 'x'), (0x20, 'y')]:
            cb = callback(None, [P, F], lambda p, value, key=key: values[panel].__setitem__(key, value))
            write(panel_vtable, offset, 'Q', C.cast(cb, P).value)
        cb = callback(None, [P, I], lambda *_: None)
        write(panel_vtable, 0x30, 'Q', C.cast(cb, P).value)
        cb = callback(None, [P, F], lambda _, value: values[child].__setitem__('h', value))
        write(child_vtable, 0x28, 'Q', C.cast(cb, P).value)
        # Panel wrappers read the dimensions of the authored nine-slice child.
        for pointer in [panel, child_wrapper]:
            vtable = C.c_uint64.from_address(pointer).value
            for offset, key in [(0x70, 'w'), (0x78, 'h')]:
                cb = callback(F, [P], lambda _, key=key: values[child][key])
                write(vtable, offset, 'Q', C.cast(cb, P).value)

        module.patch(0x863ea0, dll.bounds)
        for address in [0x863dd0, 0x863a40, 0x842f20, 0x842f90, 0x7d0970]:
            patch(address, None, [P], lambda *_: None)
        patch(0x86c920, I, [P], lambda _: 1)
        patch(0x7d5910, I, [P], lambda _: 1)
        patch(0x864ca0, F, [P], lambda _: values[panel]['x'])
        patch(0x8636d0, F, [P], lambda _: values[panel]['y'])
        patch(0x7cffe0, F, [P], lambda _: values[panel]['x'])
        patch(0x7d0700, F, [P], lambda _: values[panel]['y'])
        patch(0x7d0690, P, [P, I], lambda *_: child_wrapper)
        patch(0x7d32e0, None, [P, F], lambda _, value: values[child].__setitem__('w', value))

        guard = allocate(8)
        write(guard, 0, 'Q', 0x12345678)
        module.w(0x134bae8, 'Q', guard)
        cursor_ui, panel_ui, content = allocate(0x100), allocate(0x100), allocate(0x28)
        write(content, 0x20, 'Q', 7)
        write(cursor_ui, 0x40, 'Q', text)
        write(cursor_ui, 0x48, 'Q', panel_wrapper)
        write(cursor_ui, 0x58, 'Q', cursor_wrapper)
        write(panel_ui, 0x40, 'Q', text)
        write(panel_ui, 0x48, 'Q', panel)
        write(text, 0x138, 'Q', 7)
        write(text, 0x14c, 'i', 1)
        # The widget's original virtual visibility setter belongs to the raw node.
        vtable = C.c_uint64.from_address(panel).value
        write(vtable, 0x30, 'Q', C.cast(callback(None, [P, I], lambda *_: None), P).value)
        module.trap_unbound()
        result = []
        for width, height in [(20., 20.), (200., 20.), (200., 80.), (399., 140.)]:
            for glyph_height in [13., 18., 20., 26.8]:
                glyph.value = glyph_height
                for kind, function, pointer in [('cursor', 0x7d57c0, cursor_ui), ('trading', 0x864f80, panel_ui)]:
                    for value in values.values():
                        value.update(x=0., y=0., w=0., h=0.)
                    values[cursor]['w'] = 48.
                    values[text].update(w=width, h=height)
                    module.function(function, None, [P, P])(pointer, content)
                    result.append(dict(kind=kind, width=width, height=height, glyph_height=glyph_height,
                                       panel_width=values[child]['w'], panel_height=values[child]['h'],
                                       inset_x=values[text]['x'] - values[panel]['x'],
                                       inset_y=values[text]['y'] - values[panel]['y']))
        return dict(repository='Nejirus/mcps4', revision='37e258085773f11599cf0ffe29119926769554b7',
                    eboot_sha256=hashlib.sha256(eboot.read_bytes()).hexdigest(),
                    routines={'cursor': '0x7d57c0 / 0x7d5920', 'trading': '0x864f80'},
                    inputs='Explicit character-zero glyph extents and text-field dimensions; isolated UI callbacks.',
                    cases=result)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--eboot', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    data = capture(args.eboot)
    args.output.write_text(json.dumps(data, indent=2) + '\n')
    print('Captured', len(data['cases']), 'original pointer-panel layouts')
