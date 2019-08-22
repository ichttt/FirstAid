/*
 * FirstAid
 * Copyright (C) 2017-2019
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

package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.Hand;
import net.minecraftforge.client.ForgeIngameGui;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.lwjgl.glfw.GLFW;


public class ClientHooks {
    public static final KeyBinding showWounds = new KeyBinding("keybinds.show_wounds", KeyConflictContext.UNIVERSAL, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_H), "First Aid");

    public static void setup(FMLClientSetupEvent event) {
        FirstAid.LOGGER.debug("Loading ClientHooks");
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        ClientRegistry.registerKeyBinding(showWounds);
        ForgeIngameGui.renderHealth = FirstAidConfig.CLIENT.showVanillaHealthBar.get();
        EventCalendar.checkDate();
    }

    public static void lateSetup(FMLLoadCompleteEvent event) { //register after the reload listener for language has registered
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(HUDHandler.INSTANCE);
    }

    public static void showGuiApplyHealth(Hand activeHand) {
        Minecraft mc = Minecraft.getInstance();
        GuiHealthScreen.INSTANCE = new GuiHealthScreen(CommonUtils.getDamageModel(mc.player), activeHand);
        mc.displayGuiScreen(GuiHealthScreen.INSTANCE);
    }
}
