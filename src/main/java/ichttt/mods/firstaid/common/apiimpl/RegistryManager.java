/*
 * FirstAid
 * Copyright (C) 2017-2023
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

package ichttt.mods.firstaid.common.apiimpl;

import cpw.mods.modlauncher.TransformingClassLoader;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.event.RegisterHealingTypeEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class RegistryManager {
    public static final List<String> debuffConfigErrors = new ArrayList<>();

    public static void registerAndValidate() {
        MinecraftForge.EVENT_BUS.register(RegistryManager.class);
        //Validate everything is on the same TCL, otherwise things might break
        if (RegistryManager.class.getClassLoader() != FirstAidRegistry.class.getClassLoader()) {
            FirstAid.LOGGER.error("API and normal mod loaded on two different classloaders! Normal mod: {}, First Aid Registry: {}", RegistryManager.class.getName(), FirstAidRegistry.class.getName());
            throw new RuntimeException("API and normal mod loaded on two different classloaders!");
        }
        TransformingClassLoader tcl = (TransformingClassLoader) RegistryManager.class.getClassLoader();
        if (tcl.getLoadedClass(RegistryManager.class.getName()) != RegistryManager.class) {
            FirstAid.LOGGER.error("API is not the same as under tcl loaded classes! In TCL cache: {}, actual: {}", tcl.getLoadedClass(RegistryManager.class.getName()), RegistryManager.class);
            throw new RuntimeException("API is not under loaded classes in the TCL!");
        }
    }

    public static void fireRegistryEvents(Level level) {
        if (FirstAidRegistry.getImpl() != null) FirstAid.LOGGER.warn("A registry has already been set!");
        RegisterHealingTypeEvent registerHealingTypeEvent = new RegisterHealingTypeEvent(level);
        MinecraftForge.EVENT_BUS.post(registerHealingTypeEvent);
        FirstAidRegistryImpl impl = new FirstAidRegistryImpl(registerHealingTypeEvent.getHealerMap());

        FirstAidRegistry.setImpl(impl);
    }

    public static void destroyRegistry() {
        if (FirstAidRegistry.getImpl() == null) FirstAid.LOGGER.warn("No registry has been set!");
        FirstAidRegistry.setImpl(null);
    }

    private static void logError(String error, String potionName, EnumDebuffSlot slot) {
        String errorMsg = String.format("Invalid config entry for debuff %s at part %s: %s", potionName, slot.toString(), error);
        FirstAid.LOGGER.warn(errorMsg);
        debuffConfigErrors.add(errorMsg);
    }
}
