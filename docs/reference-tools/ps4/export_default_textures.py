"""Translate the pinned PS4 default atlas registrations into named Java textures.

The registration arguments come from the original default-pack branch in
TextureAtlas initialization, not from visual guesses about the sheet layout.
"""
import argparse
import hashlib
import json
from pathlib import Path
from io import BytesIO
import zipfile
from PIL import Image
from capstone.x86 import X86_OP_IMM, X86_OP_REG, X86_OP_MEM, X86_REG_RIP
from read_block_constructors import Source

# Registry/filename changes between the console atlas and Java 1.17.1. These
# bind the same named material/item; no image is drawn, recolored or rescaled.
BLOCK_NAMES = {
    'grass_top': 'grass_block_top', 'grass_side': 'grass_block_side',
    'snow_side': 'grass_block_snow', 'grass_side_snowed': 'grass_block_snow',
    'grass_side_overlay': 'grass_block_side_overlay',
    'quartz_ore': 'nether_quartz_ore',
    'leaves': 'oak_leaves', 'leaves_jungle': 'jungle_leaves', 'workbench_side': 'crafting_table_side',
    'workbench_front': 'crafting_table_front', 'door_wood_upper': 'oak_door_top',
    'door_wood_lower': 'oak_door_bottom', 'trip_wire': 'tripwire',
    'nether_brick': 'nether_bricks', 'stone_diorite': 'diorite',
    'log_jungle_top': 'jungle_log_top', 'double_plant_syringa_top': 'lilac_top',
    'double_plant_syringa_bottom': 'lilac_bottom', 'leaves_acacia': 'acacia_leaves',
    'leaves_big_oak': 'dark_oak_leaves', 'leaves_birch': 'birch_leaves',
    'observer_back_lit': 'observer_back_on', 'coral_purple': 'bubble_coral',
    'coral_red': 'fire_coral', 'bamboo_leaf_small': 'bamboo_small_leaves',
    'bamboo_leaf': 'bamboo_large_leaves', 'stripped_log_dark_oak': 'stripped_dark_oak_log',
    'stripped_log_spruce': 'stripped_spruce_log',
    **{'destroy_' + str(i): 'destroy_stage_' + str(i) for i in range(10)},
    **{'glass_pane_top_' + color: ('light_gray' if color == 'silver' else color) + '_stained_glass_pane_top'
       for color in ['black', 'blue', 'brown', 'cyan', 'gray', 'green', 'light_blue', 'lime', 'magenta',
                     'orange', 'pink', 'purple', 'red', 'silver', 'white', 'yellow']},
}
ITEM_NAMES = {
    **{old + material: material_name + '_' + new
       for old, new in [('helmet', 'helmet'), ('chestplate', 'chestplate'), ('leggings', 'leggings'), ('boots', 'boots')]
       for material, material_name in [('Cloth', 'leather'), ('Chain', 'chainmail'), ('Iron', 'iron'), ('Gold', 'golden'), ('Diamond', 'diamond')]},
    **{old + material: material_name + '_' + new
       for old, new in [('sword', 'sword'), ('shovel', 'shovel'), ('pickaxe', 'pickaxe'), ('hatchet', 'axe'), ('hoe', 'hoe')]
       for material, material_name in [('Wood', 'wooden'), ('Stone', 'stone'), ('Iron', 'iron'), ('Gold', 'golden'), ('Diamond', 'diamond')]},
    'reeds': 'sugar_cane', 'potion_contents': 'potion_overlay',
    'gold_horse_armor': 'golden_horse_armor', 'monsterPlacer': 'spawn_egg',
    'fireworks_charge': 'firework_star', 'map_filled_markings': 'filled_map_markings',
    'leather_horse_armor_base': 'leather_horse_armor',
}
ENTITY_NAMES = {
    'mob/dolphin.png': 'dolphin.png', 'mob/phantom.png': 'phantom.png',
    'mob/sea_turtle.png': 'turtle/big_sea_turtle.png',
    'mob/zombie/drowned.png': 'zombie/drowned.png',
    'mob/cat/tuxedo.png': 'cat/black.png',
    'mob/illager/vex_charging.png': 'illager/vex_charging.png',
    'mob/llama/decor/decor_yellow.png': 'llama/decor/yellow.png',
    **{'mob/horse/' + name + '.png': 'horse/' + name + '.png'
       for name in ['donkey', 'mule', 'horse_creamy', 'horse_skeleton']},
    **{'mob/horse/armor/horse_armor_' + material + '.png': 'horse/armor/horse_armor_' + material + '.png'
       for material in ['diamond', 'gold', 'iron']},
    **{'mob/fish/' + name + '.png': 'fish/' + name + '.png'
       for name in ['cod', 'tropical_a', 'tropical_b'] +
       ['tropical_' + body + '_pattern_' + str(i) for body in ['a', 'b'] for i in range(1, 7)]},
}

def registrations(eboot):
    source = Source(eboot)
    registers, strings, result = {'rbp': 0}, {}, []

    def value(instruction, operand):
        if operand.type == X86_OP_IMM:
            return operand.imm
        if operand.type == X86_OP_REG:
            return registers.get(source.register(operand.reg))
        return None

    # Keep only resolved literal-name calls in the original initializer. Calls
    # whose names come from a selected external pack are not exportable here.
    for instruction in source.instructions(0xd3e070, 0xd53b40 - 0xd3e070):
        operands = instruction.operands
        opcode = instruction.mnemonic
        if opcode in ('mov', 'movabs', 'movzx', 'movsxd') and operands[0].type == X86_OP_REG:
            registers[source.register(operands[0].reg)] = value(instruction, operands[1])
        elif opcode == 'lea' and operands[0].type == X86_OP_REG:
            memory = operands[1].mem
            base = instruction.address + instruction.size if memory.base == X86_REG_RIP else registers.get(source.register(memory.base))
            registers[source.register(operands[0].reg)] = base + memory.disp if isinstance(base, int) else None
        elif opcode == 'xor' and operands[0].type == operands[1].type == X86_OP_REG and operands[0].reg == operands[1].reg:
            registers[source.register(operands[0].reg)] = 0
        elif opcode == 'call' and operands[0].type == X86_OP_IMM:
            address = operands[0].imm
            if address == 0x67840:
                pointer, count, target = (registers.get(k) for k in ('rsi', 'rdx', 'rdi'))
                strings[target] = None
                if isinstance(pointer, int) and isinstance(count, int) and 0 <= count < 512:
                    try:
                        strings[target] = source.read(pointer, count * 2 if count else 256).decode('utf-16le').split('\0')[0]
                    except (ValueError, UnicodeDecodeError):
                        pass
            elif address in (0xd549d0, 0xd54bf0, 0xd54e20):
                name = strings.get(registers.get('r8'))
                if name:
                    result.append(dict(pc=hex(instruction.address), routine=hex(address),
                                       atlas=registers.get('rsi'), row=registers.get('rdx'),
                                       column=registers.get('rcx'), name=name))
    return result


def visible_pixels(image):
    """Ignore RGB under zero alpha for matching only; exports keep those bytes."""
    image = image.convert('RGBA')
    return (image.size, bytes(channel for pixel in image.get_flattened_data()
                              for channel in (pixel if pixel[3] else (0, 0, 0, 0))))


def export(eboot, resource_root, vanilla, programmer_art, fixes, project):
    entries = registrations(eboot)
    manifest_path = project / 'src/main/resources/assets/legacyconsole/ps4_default_textures.json'
    previous = json.loads(manifest_path.read_text())['textures'] if manifest_path.exists() else {}
    vanilla_zip, catalog_zip = zipfile.ZipFile(vanilla), zipfile.ZipFile(programmer_art)
    allowed = ('block/', 'item/', 'entity/', 'models/', 'painting/', 'environment/', 'particle/', 'misc/')
    prefix = 'assets/minecraft/textures/'
    paths = {name for name in vanilla_zip.namelist() if name.startswith(prefix) and
             name[len(prefix):].startswith(allowed) and name.endswith('.png')}
    dimensions, catalog = {}, {}
    for archive in (vanilla_zip, catalog_zip):
        for name in sorted(paths.intersection(archive.namelist())):
            image = Image.open(BytesIO(archive.read(name))).convert('RGBA')
            if archive is vanilla_zip:
                dimensions[name] = image.size
            if image.size == dimensions[name]:
                catalog.setdefault(visible_pixels(image), set()).add(name)
    output = {}

    def emit(target, image, evidence, priority):
        if target in output and output[target]['priority'] > priority:
            return
        image = image.convert('RGBA')
        buffer = BytesIO()
        image.save(buffer, format='PNG', optimize=True, compress_level=9)
        payload = buffer.getvalue()
        assert Image.open(BytesIO(payload)).convert('RGBA').tobytes() == image.tobytes()
        output[target] = dict(priority=priority, payload=payload, evidence=evidence,
                              pixels_sha256=hashlib.sha256(image.tobytes()).hexdigest())

    atlases = {i: Image.open(resource_root / file).convert('RGBA')
               for i, file in [(0, 'items.png'), (1, 'terrain.png')]}
    native_files = {str(file.relative_to(resource_root)).replace('\\', '/'):
                    hashlib.sha256(file.read_bytes()).hexdigest()
                    for file in resource_root.rglob('*') if file.is_file()}
    missing = []
    for entry in entries:
        if entry['routine'] != '0xd549d0':
            continue  # Procedural/animated console textures require renderer adapters.
        atlas = atlases[entry['atlas']]
        x, y = entry['column'] * 16, entry['row'] * 16
        assert 0 <= x <= atlas.width - 16 and 0 <= y <= atlas.height - 16
        image = atlas.crop((x, y, x + 16, y + 16))
        if not any(pixel[3] for pixel in image.get_flattened_data()):
            continue
        evidence = dict(provider='mcps4', file='items.png' if entry['atlas'] == 0 else 'terrain.png',
                        registration=entry, rectangle=[x, y, 16, 16])
        targets = sorted(catalog.get(visible_pixels(image), set()))
        for target in targets:
            emit(target, image, dict(evidence, mapping='Identical visible pixels in the Java texture catalog'), 20)
        names = ITEM_NAMES if entry['atlas'] == 0 else BLOCK_NAMES
        renamed = names.get(entry['name'], entry['name'])
        target = prefix + ('item/' if entry['atlas'] == 0 else 'block/') + renamed + '.png'
        # A single static console cell must not replace an animated Java strip.
        if target in paths and dimensions[target] == (16, 16):
            emit(target, image, dict(evidence, mapping='Registered texture name / Java registry filename', target_name=renamed), 30)
            targets.append(target)
        if not targets:
            missing.append(entry)
    unmatched_images = []
    for file in sorted(resource_root.rglob('*.png')):
        if file.parent == resource_root:
            continue
        image = Image.open(file).convert('RGBA')
        targets = sorted(catalog.get(visible_pixels(image), set()))
        evidence = dict(provider='mcps4', file=str(file.relative_to(resource_root)),
                        mapping='Identical visible pixels and dimensions in the Java texture catalog')
        for target in targets:
            emit(target, image, evidence, 20)
        relative = str(file.relative_to(resource_root))
        if relative in ENTITY_NAMES:
            target = prefix + 'entity/' + ENTITY_NAMES[relative]
            assert target in paths and dimensions[target] == image.size, relative
            reference = Image.open(BytesIO(catalog_zip.read(target))).convert('RGBA')
            pairs = list(zip(image.get_flattened_data(), reference.get_flattened_data()))
            same_colors = all(a[:3] == b[:3] for a, b in pairs if b[3])
            same_mask = all((a[3] > 127) == (b[3] > 127) for a, b in pairs)
            if same_colors:
                # Some console opaque-material textures have alpha unsuitable
                # for Java's cutout pass. Use the existing compatible coverage
                # only when every visible RGB pixel has an exact PS4 UV match.
                converted = image.copy()
                converted.putalpha(reference.getchannel('A'))
                emit(target, converted, dict(evidence,
                     mapping='Explicit entity filename; every Java-visible RGB pixel equals the original PS4 pixel at the same UV',
                     alpha_adapter='Existing Java Programmer Art coverage for the Java cutout renderer',
                     alpha_source_sha256=hashlib.sha256(catalog_zip.read(target)).hexdigest()), 30)
                targets.append(target)
            elif same_mask:
                emit(target, image, dict(evidence,
                     mapping='Explicit entity filename; identical UV occupancy and original dimensions'), 30)
                targets.append(target)
        if not targets:
            unmatched_images.append(str(file.relative_to(resource_root)))
    with zipfile.ZipFile(fixes) as archive:
        fixed = []
        for name in sorted(archive.namelist()):
            if name.startswith(prefix + 'block/') and name.endswith('.png') and ('_ore' in name or 'deepslate' in name) and not name.endswith(('/nether_quartz_ore.png', '/nether_gold_ore.png')):
                assert name in paths, name
                payload = archive.read(name)
                image = Image.open(BytesIO(payload)).convert('RGBA')
                output[name] = dict(priority=100, payload=payload,
                                    pixels_sha256=hashlib.sha256(image.tobytes()).hexdigest(),
                                    evidence=dict(provider='Programmer Art Fix rv4', file=name,
                                                  file_sha256=hashlib.sha256(payload).hexdigest()))
                fixed.append(name)
        copyright_text = archive.read('copyright.txt')
    # These are explicit user exceptions: Java's Nether gold and zombie pigmen.
    for name in [prefix + 'block/nether_gold_ore.png', prefix + 'entity/piglin/zombified_piglin.png']:
        output.pop(name, None)
    metadata = {}
    for name in output:
        companion = name + '.mcmeta'
        if companion in vanilla_zip.namelist():
            payload = vanilla_zip.read(companion)
            metadata[companion] = dict(payload=payload, sha256=hashlib.sha256(payload).hexdigest(),
                                       provider='Minecraft 1.17.1 animation metadata adapter')
    resource_output = project / 'src/main/resources'
    for old in previous.keys() - output.keys():
        (resource_output / old).unlink(missing_ok=True)
    for target, record in output.items():
        file = resource_output / target
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_bytes(record['payload'])
    for name, record in metadata.items():
        (resource_output / name).write_bytes(record['payload'])
    credits = project / 'docs/resource-pack'
    credits.mkdir(parents=True, exist_ok=True)
    (credits / 'ProgrammerArtFix-copyright.txt').write_bytes(copyright_text)
    manifest = dict(repository='Nejirus/mcps4', revision='37e258085773f11599cf0ffe29119926769554b7',
                    eboot_sha256=hashlib.sha256(eboot.read_bytes()).hexdigest(),
                    registration_routine='0xd3e070; cell constructor 0xd549d0',
                    transform='Native 16x16 atlas cells; lossless PNG encoding. Explicit entity alpha adapters below preserve every visible PS4 RGB value. No recoloring or new artwork.',
                    catalog='Java/Programmer Art files identify UV-compatible paths by identical visible pixels; their artwork is not copied.',
                    catalog_sha256=hashlib.sha256(programmer_art.read_bytes()).hexdigest(),
                    fixes=dict(name=fixes.name, sha256=hashlib.sha256(fixes.read_bytes()).hexdigest(),
                               author='THGABS', url='https://modrinth.com/resourcepack/programmer-art-fix', files=fixed),
                    source_files=native_files, registrations=entries,
                    textures={name: dict(record['evidence'], sha256=hashlib.sha256(record['payload']).hexdigest(),
                                         pixels_sha256=record['pixels_sha256']) for name, record in sorted(output.items())},
                    animation_metadata={name: {k: v for k, v in record.items() if k != 'payload'}
                                        for name, record in sorted(metadata.items())},
                    unmapped_atlas=missing, unmapped_images=unmatched_images,
                    adapters=['Java animations and textures without an established compatible PS4 mapping retain their existing renderer/assets.',
                              'Supplied ore/deepslate PNGs override PS4 cells except Nether quartz (original PS4) and Nether gold (default Java), as requested.',
                              'Java zombie pigmen retain their existing model and textures.',
                              'Animated PNG overrides include their Java animation metadata in the same resource pack.'])
    file = resource_output / 'assets/legacyconsole/ps4_default_textures.json'
    file.parent.mkdir(parents=True, exist_ok=True)
    file.write_text(json.dumps(manifest, indent=2) + '\n')
    return manifest


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--eboot', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--resources', type=Path)
    parser.add_argument('--vanilla', type=Path)
    parser.add_argument('--catalog', type=Path)
    parser.add_argument('--fixes', type=Path)
    parser.add_argument('--project', type=Path)
    args = parser.parse_args()
    if args.project:
        data = export(args.eboot, args.resources, args.vanilla, args.catalog, args.fixes, args.project)
        args.output.write_text(json.dumps(dict(textures=len(data['textures']), supplied_fixes=len(data['fixes']['files']),
                                               unmapped_atlas=len(data['unmapped_atlas']), unmapped_images=len(data['unmapped_images'])), indent=2) + '\n')
    else:
        args.output.write_text(json.dumps(registrations(args.eboot), indent=2) + '\n')
