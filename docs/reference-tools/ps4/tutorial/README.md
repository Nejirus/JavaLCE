# Tutorial reference tools

These operate on separately obtained, hash-verified files from the revision in
`docs/MCPS4-TUTORIAL-237.json`. The executable/media archive are not bundled.
Python dependencies: numpy, capstone, unicorn, Pillow (the existing FUI tools).
Use Java 17 and the project's ForgeGradle classpath for the schema conversion.

Set `MCPS4_EBOOT` to the original executable, `PS4_TUTORIAL_WORK` to a scratch
folder, and `PS4_ENGLISH_JSON` to the hash-keyed English table exported by
`../read_languages.py` from the original `languages.loc`. Add the parent
reference-tools directory to PYTHONPATH when using its existing native helpers.

1. `extract_container.py --mcs /path/Tutorial.mcs --pck /path/Tutorial.pck --output /scratch`
   extracts `raw-world`, the original GRF rule JSON and `reference/Tutorial.mcs`.
2. `convert_world.py` transposes/re-buckets chunks into `anvil-intermediate-v2`
   and applies the source initialization rules. It preserves source saved data.
3. Set `PS4_TUTORIAL_CONVERTER` to this directory, then run Gradle with
   `-I docs/reference-tools/ps4/tutorial/convert.init.gradle convertPs4Tutorial`.
   This writes `java-world-v2` through Minecraft's DataFixer. The optional
   `-PtutorialMetadataOnly` leaves converted region files intact.
4. Zip the contents of `java-world-v2` at the archive root for the template.
   Keep the `data/legacyconsole_ps4_tutorial.dat` marker and dimension datapack.

`capture_tutorial.py full` and `capture_tutorial.py tasks-guided` execute the
isolated original constructor paths. `export_tutorial_tasks.py` normalizes the
included snapshots; its result is the bundled `tutorial/tasks.json`.
`export_descriptions.py` observes actual registry description setter arguments;
`export_stone_variants.py` observes stone enum fields. Registry helpers and
native-module loader live in the parent directory. No gameplay services run.

`base` hint capture is partial and is not represented as a complete hint graph.
The provided generic-hint resource has only the three verified basic hints.
`popup-source.json` records the original authored FUI nodes, independent of
Java's font/text fitting. `converted-audit.json` records all 3,384 full chunks,
entity/block-entity types, and the absence of unconverted item stacks.
