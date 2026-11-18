// LCE has collision at its finite ocean edge and no visible wall geometry.
// Transform only the wall draw method. Avoid asking Mixin 0.8.4 to inspect
// LevelRenderer's Java 17/OptiFine hierarchy (ASM8 cannot read sealed classes).
function initializeCoreMod() {
    var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
    var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
    var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
    return {
        legacyconsole_ocean_border: {
            target: { type: 'CLASS', name: 'net.minecraft.client.renderer.LevelRenderer' },
            transformer: function(node) {
                var mapped = ASMAPI.mapMethod('m_173012_');
                for (var i = 0; i < node.methods.size(); i++) {
                    var method = node.methods.get(i);
                    if ((method.name === mapped || method.name === 'renderWorldBorder') &&
                            method.desc === '(Lnet/minecraft/client/Camera;)V') {
                        var original = new LabelNode();
                        var instructions = new InsnList();
                        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            'dev/legacyconsole/client/LegacyWorldView', 'active', '()Z', false));
                        instructions.add(new JumpInsnNode(Opcodes.IFEQ, original));
                        instructions.add(new InsnNode(Opcodes.RETURN));
                        instructions.add(original);
                        method.instructions.insert(instructions);
                        return node;
                    }
                }
                throw new Error('Legacy Console: world-border render method not found');
            }
        }
    };
}
