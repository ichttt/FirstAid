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

import ichttt.mods.firstaid.common.asm.FirstAidCoremod;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class AbstractMethodTransformer implements IClassTransformer, Opcodes {
    public final String className;
    public final String srgName;
    public final String mcpName;
    public final String methodDesc;
    private final int computeFlags;

    public AbstractMethodTransformer(String className, String srgName, String mcpName, String methodDesc, int computeFlags) {
        this.className = className;
        this.srgName = srgName;
        this.mcpName = mcpName;
        this.methodDesc = methodDesc;
        this.computeFlags = computeFlags;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!transformedName.equals(className))
            return basicClass;
        try {
            return findMethodAndPatch(name, transformedName, basicClass);
        } catch (PatchFailedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new PatchFailedException(className, "No further details available, check log", e);
        }
    }

    private byte[] findMethodAndPatch(String name, String transformedName, byte[] basicClass) {
        FirstAidCoremod.LOGGER.info("Starting patch of class {}", name);
        ClassNode node = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(node, 0);
        boolean found = false;
        for (MethodNode method : node.methods) {
            if ((method.name.equals(srgName) || method.name.equals(mcpName)) && method.desc.equals(methodDesc))  {
                FirstAidCoremod.LOGGER.debug("Found correct method {} with desc {}", this.mcpName, this.methodDesc);
                found = true;
                if (patchMethod(method)) {
                    FirstAidCoremod.LOGGER.debug("Patch seems to be successful!");
                    break;
                } else {
                    FirstAidCoremod.LOGGER.fatal("PATCH FAILED!");
                    throw new PatchFailedException(transformedName, "The Subtansformer signalized that it could not find the method it's entry point, see log for further info");
                }
            }
        }
        if (!found) {
            FirstAidCoremod.LOGGER.fatal("Did not find method (mcp: {} srg: {}) with desc {}", mcpName, srgName, methodDesc);
            throw new PatchFailedException(transformedName, "The method " + mcpName + " with the desc " + methodDesc + " could not be found");
        }

        ClassWriter writer = new ClassWriter(computeFlags);
        node.accept(writer);
        byte[] data = writer.toByteArray();
        FirstAidCoremod.LOGGER.info("Patch of class {} ended", name);
        return data;
    }

    protected abstract boolean patchMethod(MethodNode node) throws PatchFailedException;
}
