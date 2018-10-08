/*
 * FirstAid
 * Copyright (C) 2017-2018
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.common.asm.framework;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASMUtils {

    private ASMUtils() {}

    public static boolean matchMethodNode(int opcode, String owner, String nameSRG, String nameMCP, String desc, AbstractInsnNode node) {
        if (node.getOpcode() == opcode) {
            MethodInsnNode castedNode = (MethodInsnNode) node;
            return (castedNode.name.equals(nameSRG) || castedNode.name.equals(nameMCP)) && castedNode.desc.equals(desc) && castedNode.owner.equals(owner);
        }
        return false;
    }

    public static boolean matchVarNode(int opcode, int var, AbstractInsnNode node) {
        if (node.getOpcode() == opcode) {
            VarInsnNode castedNode = (VarInsnNode) node;
            return castedNode.var == var;
        }
        return false;
    }

    public static boolean matchFieldNode(int opcode, String owner, String nameSRG, String nameMCP, String desc, AbstractInsnNode node) {
        if (node.getOpcode() == opcode) {
            FieldInsnNode castedNode = (FieldInsnNode) node;
            return (castedNode.name.equals(nameSRG) || castedNode.name.equals(nameMCP)) && castedNode.desc.equals(desc) && castedNode.owner.equals(owner);
        }
        return false;
    }

    public static String nodeToString(AbstractInsnNode node) {
        int type = node.getType();
        int opcode = node.getOpcode();
        switch (type) {
            case AbstractInsnNode.INSN:
                return String.format("InsnNode (type %d), Opcode %d", type, opcode);
            case AbstractInsnNode.INT_INSN:
                return String.format("IntInsnNode (type %d), Opcode %d, Operand %d", type, opcode, ((IntInsnNode) node).operand);
            case AbstractInsnNode.VAR_INSN:
                return String.format("VarInsnNode (type %d), Opcode %d, Var %d", type, opcode, ((VarInsnNode) node).var);
            case AbstractInsnNode.TYPE_INSN:
                return String.format("TypeInsnNode (type %d), Opcode %d, Desc %s", type, opcode, ((TypeInsnNode) node).desc);
            case AbstractInsnNode.FIELD_INSN:
                return String.format("FieldInsnNode (type %d), Opcode %d, Owner %s, Name %s, Desc %s", type, opcode, ((FieldInsnNode) node).owner, ((FieldInsnNode) node).name, ((FieldInsnNode) node).desc);
            case AbstractInsnNode.METHOD_INSN:
                return String.format("MethodInsnNode (type %d), Opcode %d, Owner %s, Name %s, Desc %s, Interface %b", type, opcode, ((MethodInsnNode) node).owner, ((MethodInsnNode) node).name, ((MethodInsnNode) node).desc, ((MethodInsnNode) node).itf);
            case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                return String.format("InvokeDynamicInsnNode (type %d), Opcode %d, Name %s, Desc %s", type, opcode, ((InvokeDynamicInsnNode) node).name, ((InvokeDynamicInsnNode) node).desc);
            default:
                return String.format("UnknownNode (type %d), Opcode %d, (%s)", type, opcode, node.toString());
        }
    }
}
