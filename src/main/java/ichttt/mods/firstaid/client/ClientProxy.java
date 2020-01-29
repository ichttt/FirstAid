/*
 * FirstAid
 * Copyright (C) 2017-2020
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
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.common.IProxy;
import ichttt.mods.firstaid.common.config.ConfigEntry;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import ichttt.mods.firstaid.common.config.ExtraConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ClientProxy implements IProxy {
    public static final KeyBinding showWounds = new KeyBinding("keybinds.show_wounds", KeyConflictContext.IN_GAME, Keyboard.KEY_H, FirstAid.NAME);
    public static List<ConfigEntry<ExtraConfig.Advanced>> advancedConfigOptions;

    @Override
    public void preInit() {
        FirstAid.LOGGER.debug("Loading ClientProxy");
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        ClientRegistry.registerKeyBinding(showWounds);
    }

    @Override
    public void init() {
        GuiIngameForge.renderHealth = FirstAidConfig.overlay.showVanillaHealthBar;
        EventCalendar.checkDate();
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(HUDHandler.INSTANCE);
        advancedConfigOptions = ExtraConfigManager.getAnnotatedFields(ExtraConfig.Advanced.class, FirstAidConfig.class);
    }

    @Override
    public void showGuiApplyHealth(EnumHand activeHand) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiHealthScreen.INSTANCE = new GuiHealthScreen(mc.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null), activeHand);
        mc.displayGuiScreen(GuiHealthScreen.INSTANCE);
    }
}
