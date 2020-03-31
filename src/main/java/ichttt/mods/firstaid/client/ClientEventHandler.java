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
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.tutorial.GuiTutorial;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.client.util.PlayerModelRenderer;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.apiimpl.RegistryManager;
import ichttt.mods.firstaid.common.config.ConfigEntry;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    private static int id;

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.BANDAGE, 0, new ModelResourceLocation("firstaid:bandage"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.PLASTER, 0, new ModelResourceLocation("firstaid:plaster"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.MORPHINE, 0, new ModelResourceLocation("firstaid:morphine"));
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || mc.player.connection == null || mc.isGamePaused()) return;
        if (EventCalendar.isGuiFun()) {
            GuiHealthScreen.BED_ITEMSTACK.setItemDamage(id);
            if (mc.world != null && mc.world.getWorldTime() % 3 == 0) id++;
            if (id > 15) id = 0;
            PlayerModelRenderer.tickFun();
        }
        if (!RegistryManager.debuffConfigErrors.isEmpty() && mc.world.isRemote) {
            mc.player.sendStatusMessage(new TextComponentString("[FirstAid] FirstAid has detected invalid debuff config entries."), false);
            for (String s : RegistryManager.debuffConfigErrors)
                mc.player.sendStatusMessage(new TextComponentString("[FirstAid] " + s), false);
            RegistryManager.debuffConfigErrors.clear();
        }
        if (HUDHandler.INSTANCE.ticker >= 0)
            HUDHandler.INSTANCE.ticker--;
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        if (ClientProxy.showWounds.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            AbstractPlayerDamageModel damageModel = Objects.requireNonNull(mc.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
            if (!damageModel.hasTutorial) {
                damageModel.hasTutorial = true;
                CapProvider.tutorialDone.add(mc.player.getName());
                Minecraft.getMinecraft().displayGuiScreen(new GuiTutorial());
            }
            else {
                mc.displayGuiScreen(new GuiHealthScreen(damageModel));
            }
        }
    }

    @SubscribeEvent
    public static void preRender(RenderGameOverlayEvent.Pre event) {
        RenderGameOverlayEvent.ElementType type = event.getType();
        if (type == RenderGameOverlayEvent.ElementType.HEALTH && !FirstAidConfig.overlay.showVanillaHealthBar) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent.Post event) {
        RenderGameOverlayEvent.ElementType type = event.getType();
        if (type == RenderGameOverlayEvent.ElementType.ALL || (type == RenderGameOverlayEvent.ElementType.TEXT && FirstAidConfig.overlay.overlayMode != FirstAidConfig.Overlay.OverlayMode.OFF && FirstAidConfig.overlay.pos == FirstAidConfig.Overlay.Position.BOTTOM_LEFT)) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.profiler.startSection("FirstAidOverlay");
            GuiIngameForge.renderHealth = FirstAidConfig.overlay.showVanillaHealthBar;
            HUDHandler.INSTANCE.renderOverlay(event.getResolution(), event.getPartialTicks());
            mc.profiler.endSection();
            mc.profiler.endSection();
        }
    }

    private static String makeArmorMsg(double value) {
        return TextFormatting.BLUE + I18n.format("firstaid.specificarmor", FORMAT.format(value)) + TextFormatting.RESET;
    }

    private static String makeToughnessMsg(double value) {
        return TextFormatting.BLUE + I18n.format("firstaid.specifictoughness", FORMAT.format(value)) + TextFormatting.RESET;
    }

    private static <T> void replaceOrAppend(List<T> list, T search, T replace) {
        int index = list.indexOf(search);
        if (FirstAidConfig.overlay.armorTooltipMode == FirstAidConfig.Overlay.TooltipMode.REPLACE && index >= 0) {
            list.set(index, replace);
        } else {
            list.add(replace);
        }
    }

    @SubscribeEvent
    public static void tooltipItems(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        if (item == FirstAidItems.MORPHINE) {
            event.getToolTip().add(I18n.format("firstaid.tooltip.morphine", "3:30-4:30"));
            return;
        }
        if (FirstAidConfig.overlay.armorTooltipMode != FirstAidConfig.Overlay.TooltipMode.NONE) {
            boolean set = false;
            if (item instanceof ISpecialArmor) {
                ISpecialArmor armor = (ISpecialArmor) item;
                EntityPlayer player = event.getEntityPlayer();
                if (player != null) {
                    int slot = player.inventory.armorInventory.indexOf(stack);
                    if (slot == -1 && item instanceof ItemArmor)
                        slot = ((ItemArmor) item).armorType.getIndex();
                    if (slot >= 0 && slot <= 3) {
                        int displayArmor = armor.getArmorDisplay(event.getEntityPlayer(), stack, slot);
                        if (displayArmor != 0) {
                            set = true;
                            double totalArmor = ArmorUtils.applyArmorModifier(CommonUtils.ARMOR_SLOTS[slot], displayArmor);
                            String original = TextFormatting.BLUE + " " + net.minecraft.util.text.translation.I18n.translateToLocalFormatted("attribute.modifier.plus.0", FORMAT.format(displayArmor), net.minecraft.util.text.translation.I18n.translateToLocal("attribute.name.generic.armor"));
                            replaceOrAppend(event.getToolTip(), original, makeArmorMsg(totalArmor));
                        }
                    }
                }
            }
            if (item instanceof ItemArmor && !set) {
                ItemArmor armor = (ItemArmor) item;
                List<String> tooltip = event.getToolTip();

                double normalArmor = ArmorUtils.getArmor(stack, armor.getEquipmentSlot());
                double totalArmor = ArmorUtils.applyArmorModifier(armor.armorType, normalArmor);
                if (totalArmor > 0D) {
                    String original = TextFormatting.BLUE + " " + net.minecraft.util.text.translation.I18n.translateToLocalFormatted("attribute.modifier.plus.0", FORMAT.format(normalArmor), net.minecraft.util.text.translation.I18n.translateToLocal("attribute.name.generic.armor"));
                    replaceOrAppend(tooltip, original, makeArmorMsg(totalArmor));
                }

                double normalToughness = ArmorUtils.getArmorThoughness(stack, armor.getEquipmentSlot());
                double totalToughness = ArmorUtils.applyToughnessModifier(armor.armorType, normalToughness);
                if (totalToughness > 0D) {
                    String original = TextFormatting.BLUE + " " + net.minecraft.util.text.translation.I18n.translateToLocalFormatted("attribute.modifier.plus.0", FORMAT.format(normalToughness), net.minecraft.util.text.translation.I18n.translateToLocal("attribute.name.generic.armorToughness"));
                    replaceOrAppend(tooltip, original, makeToughnessMsg(totalToughness));
                }
            }
        }

        AbstractPartHealer healer = FirstAidRegistryImpl.INSTANCE.getPartHealer(stack);
        if (healer != null) {
            event.getToolTip().add(I18n.format("firstaid.tooltip.healer", healer.maxHeal / 2, StringUtils.ticksToElapsedTime(healer.ticksPerHeal)));
        }
    }

    @SubscribeEvent
    public static void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        FirstAid.isSynced = false;
        for (ConfigEntry<ExtraConfig.Sync> option : FirstAid.syncedConfigOptions) {
            if (option.hasRemoteData())
                option.revert();
        }
        HUDHandler.INSTANCE.ticker = -1;
    }
}
