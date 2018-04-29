package ichttt.mods.firstaid.common.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;

public class PotionTransformer extends AbstractMethodTransformer {
    public PotionTransformer() {
        super("net.minecraft.potion.Potion", "func_76394_a", "performEffect", "(Lnet/minecraft/entity/EntityLivingBase;I)V", ClassWriter.COMPUTE_FRAMES);
    }

    @Override
    protected boolean patchMethod(MethodNode methodNode) throws PatchFailedException {
        ListIterator<AbstractInsnNode> nodeIterator = methodNode.instructions.iterator();
        int countSinceNeedle = -1;
        while (nodeIterator.hasNext()) {
            AbstractInsnNode node = nodeIterator.next();
            if (countSinceNeedle != -1) {
                if (countSinceNeedle == 0) {
                    if (node.getOpcode() != IF_ACMPNE)
                        throw new PatchFailedException(className, "Found invalid opcode for sanity check: Found node " + ASMUtils.nodeToString(node));
                } else if (countSinceNeedle > 2 && countSinceNeedle < 14) {
                    nodeIterator.remove();
                } else if (countSinceNeedle == 14) {
                    AbstractInsnNode addBefore = node.getNext();
                    nodeIterator.remove();
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new MethodInsnNode(INVOKESTATIC, "ichttt/mods/firstaid/common/asm/ASMHooks", "onPoisonEffect", "(Lnet/minecraft/entity/EntityLivingBase;)V", false));
                    methodNode.instructions.insertBefore(addBefore, list);
                    return true;
                }
                countSinceNeedle++;
            }
            if (ASMUtils.matchFieldNode(GETSTATIC, "net/minecraft/init/MobEffects", "field_76436_u", "POISON", "Lnet/minecraft/potion/Potion;", node)) {
                if (countSinceNeedle != -1)
                    throw new PatchFailedException(className, "Found the GETSTATIC(" + ASMUtils.nodeToString(node) + ") needle twice!");
                FirstAidCoremod.LOGGER.debug("Found needle GETSTATIC: " + ASMUtils.nodeToString(node));
                countSinceNeedle++;
            }
        }
        FirstAidCoremod.LOGGER.fatal("Could not find needle in haystack!");
        return false;
    }
}
