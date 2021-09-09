/*
 * FirstAid
 * Copyright (C) 2017-2021
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

import com.mojang.blaze3d.vertex.PoseStack;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;


public class ClientHooks {
    public static IIngameOverlay firstAidOverlay;
    public static final KeyMapping SHOW_WOUNDS = new KeyMapping("keybinds.show_wounds", KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_H), "First Aid");

    public static void setup(FMLClientSetupEvent event) {
        FirstAid.LOGGER.debug("Loading ClientHooks");
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        ClientRegistry.registerKeyBinding(SHOW_WOUNDS);
        EventCalendar.checkDate();
    }

    public static void lateSetup(FMLLoadCompleteEvent event) { //register after the reload listener for language has registered
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(HUDHandler.INSTANCE);
        firstAidOverlay = OverlayRegistry.registerOverlayTop("firstaid:hud", (gui, mStack, partialTicks, width, height) -> HUDHandler.INSTANCE.renderOverlay(mStack, gui, partialTicks));
        OverlayRegistry.enableOverlay(firstAidOverlay, FirstAidConfig.CLIENT.overlayMode.get() != FirstAidConfig.Client.OverlayMode.OFF);
    }

    public static void showGuiApplyHealth(InteractionHand activeHand) {
        Minecraft mc = Minecraft.getInstance();
        GuiHealthScreen.INSTANCE = new GuiHealthScreen(CommonUtils.getDamageModel(mc.player), activeHand);
        mc.setScreen(GuiHealthScreen.INSTANCE);
    }
}
