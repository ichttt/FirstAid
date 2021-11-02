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

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.gui.FirstaidIngameGui;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.tutorial.GuiTutorial;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.client.util.PlayerModelRenderer;
import ichttt.mods.firstaid.common.AABBAlignedBoundingBox;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.apiimpl.RegistryManager;
import ichttt.mods.firstaid.common.config.ConfigEntry;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.PlayerSizeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
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
import java.util.Collection;
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
        if (type == RenderGameOverlayEvent.ElementType.HEALTH) {
            FirstAidConfig.Overlay.VanillaHealthbarMode vanillaHealthBarMode = FirstAidConfig.overlay.vanillaHealthBarMode;
            if (vanillaHealthBarMode != FirstAidConfig.Overlay.VanillaHealthbarMode.NORMAL) {
                if (vanillaHealthBarMode == FirstAidConfig.Overlay.VanillaHealthbarMode.HIGHLIGHT_CRITICAL_PATH && FirstAidConfig.vanillaHealthCalculation == FirstAidConfig.VanillaHealthCalculationMode.AVERAGE_ALL) {
                    FirstaidIngameGui.renderHealthMixedCritical(Minecraft.getMinecraft().ingameGUI, event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight());
                    event.setCanceled(true);
                }
                if (vanillaHealthBarMode == FirstAidConfig.Overlay.VanillaHealthbarMode.SPLIT && FirstAidConfig.vanillaHealthCalculation == FirstAidConfig.VanillaHealthCalculationMode.AVERAGE_ALL && FirstAidConfig.maxHealthMode == FirstAidConfig.VanillaMaxHealthMode.SYNC_FIRSTAID_VANILLA) {
                    FirstaidIngameGui.renderHealthSplit(Minecraft.getMinecraft().ingameGUI, event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight());
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent.Post event) {
        RenderGameOverlayEvent.ElementType type = event.getType();
        if (type == RenderGameOverlayEvent.ElementType.ALL || (type == RenderGameOverlayEvent.ElementType.TEXT && FirstAidConfig.overlay.overlayMode != FirstAidConfig.Overlay.OverlayMode.OFF && FirstAidConfig.overlay.pos == FirstAidConfig.Overlay.Position.BOTTOM_LEFT)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (!mc.player.isEntityAlive()) return;
            mc.profiler.startSection("FirstAidOverlay");
            HUDHandler.INSTANCE.renderOverlay(event.getResolution(), event.getPartialTicks());
            mc.profiler.endSection();
            mc.profiler.endSection();
        }
    }

    @SubscribeEvent
    public static void onLivingRender(RenderLivingEvent.Post<EntityPlayer> event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityPlayer) {
            RenderManager renderDispatcher = Minecraft.getMinecraft().getRenderManager();
            if (renderDispatcher.isDebugBoundingBox()) {
                GlStateManager.pushMatrix();
                //See PlayerRenderer.getRenderOffset
                if (entity.isSneaking()) {
                    GlStateManager.translate(0D, 0.125D, 0D);
                }
                AxisAlignedBB aabb = entity.getEntityBoundingBox();


                Collection<AABBAlignedBoundingBox> allBoxes = PlayerSizeHelper.getBoxes(entity).values();
                float r = 0.25F;
                float g = 1.0F;
                float b = 1.0F;

                for (AABBAlignedBoundingBox box : allBoxes) {
                    AxisAlignedBB bbox = box.createAABB(aabb);
                    RenderGlobal.drawSelectionBoundingBox(bbox.grow(0.02D).offset(-entity.posX, -entity.posY, -entity.posZ), r, g, b, 1.0F);
                    r += 0.25F;
                    g += 0.5F;
                    b += 0.1F;

                    r %= 1.0F;
                    g %= 1.0F;
                    b %= 1.0F;
                }
                GlStateManager.popMatrix();
            }
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

                double normalArmor = ArmorUtils.getArmor(stack, armor.getEquipmentSlot(), false);
                double totalArmor = ArmorUtils.applyArmorModifier(armor.armorType, normalArmor);
                if (totalArmor > 0D) {
                    String original = TextFormatting.BLUE + " " + net.minecraft.util.text.translation.I18n.translateToLocalFormatted("attribute.modifier.plus.0", FORMAT.format(normalArmor), net.minecraft.util.text.translation.I18n.translateToLocal("attribute.name.generic.armor"));
                    replaceOrAppend(tooltip, original, makeArmorMsg(totalArmor));
                }

                double normalToughness = ArmorUtils.getArmorToughness(stack, armor.getEquipmentSlot(), false);
                double totalToughness = ArmorUtils.applyToughnessModifier(armor.armorType, normalToughness);
                if (totalToughness > 0D) {
                    String original = TextFormatting.BLUE + " " + net.minecraft.util.text.translation.I18n.translateToLocalFormatted("attribute.modifier.plus.0", FORMAT.format(normalToughness), net.minecraft.util.text.translation.I18n.translateToLocal("attribute.name.generic.armorToughness"));
                    replaceOrAppend(tooltip, original, makeToughnessMsg(totalToughness));
                }

                if (ArmorUtils.QUALITY_TOOLS_PRESENT) {
                    double qualityToolsNormalArmor = ArmorUtils.getValueFromQualityTools(SharedMonsterAttributes.ARMOR, stack);
                    double qualityToolsTotalArmor = qualityToolsNormalArmor * ArmorUtils.getArmorMultiplier(armor.armorType);
                    if (qualityToolsTotalArmor > 0D) {
                        String original = TextFormatting.BLUE + " " + net.minecraft.util.text.translation.I18n.translateToLocalFormatted("attribute.modifier.plus.0", FORMAT.format(qualityToolsNormalArmor), net.minecraft.util.text.translation.I18n.translateToLocal("attribute.name.generic.armor"));
                        replaceOrAppend(tooltip, original, makeArmorMsg(qualityToolsTotalArmor));
                    }

                    double qualityToolsNormalToughness = ArmorUtils.getValueFromQualityTools(SharedMonsterAttributes.ARMOR_TOUGHNESS, stack);
                    double qualityToolsTotalToughness = qualityToolsNormalToughness * ArmorUtils.getToughnessMultiplier(armor.armorType);
                    if (qualityToolsTotalToughness > 0D) {
                        String original = TextFormatting.BLUE + " " + net.minecraft.util.text.translation.I18n.translateToLocalFormatted("attribute.modifier.plus.0", FORMAT.format(qualityToolsNormalToughness), net.minecraft.util.text.translation.I18n.translateToLocal("attribute.name.generic.armorToughness"));
                        replaceOrAppend(tooltip, original, makeToughnessMsg(qualityToolsTotalToughness));
                    }
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
