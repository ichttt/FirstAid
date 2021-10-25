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
import ichttt.mods.firstaid.common.items.FirstAidItems;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.PlayerSizeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

public class ClientEventHandler {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    private static int id;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.player.connection == null || mc.isPaused()) return;
        if (EventCalendar.isGuiFun()) {
            GuiHealthScreen.BED_ITEMSTACK.setDamageValue(id);
            if (mc.level != null && mc.level.getGameTime() % 3 == 0) id++;
            if (id > 15) id = 0;
            PlayerModelRenderer.tickFun();
        }
        if (!RegistryManager.debuffConfigErrors.isEmpty() && mc.level != null && mc.level.isClientSide) {
            mc.player.displayClientMessage(new StringTextComponent("[FirstAid] FirstAid has detected invalid debuff config entries."), false);
            for (String s : RegistryManager.debuffConfigErrors)
                mc.player.displayClientMessage(new StringTextComponent("[FirstAid] " + s), false);
            RegistryManager.debuffConfigErrors.clear();
        }
        if (HUDHandler.INSTANCE.ticker >= 0)
            HUDHandler.INSTANCE.ticker--;
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent event) {
        if (ClientHooks.SHOW_WOUNDS.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(mc.player);
            if (!damageModel.hasTutorial) {
                damageModel.hasTutorial = true;
                CapProvider.tutorialDone.add(mc.player.getName().getString());
                Minecraft.getInstance().setScreen(new GuiTutorial(mc.player));
            }
            else {
                mc.setScreen(new GuiHealthScreen(damageModel, mc.player));
            }
        }
    }

    @SubscribeEvent
    public static void preRender(RenderGameOverlayEvent.Pre event) {
        RenderGameOverlayEvent.ElementType type = event.getType();
        if (type == RenderGameOverlayEvent.ElementType.HEALTH) {
            FirstAidConfig.Client.VanillaHealthbarMode vanillaHealthBarMode = FirstAidConfig.CLIENT.vanillaHealthBarMode.get();
            if (vanillaHealthBarMode != FirstAidConfig.Client.VanillaHealthbarMode.NORMAL) {
                event.setCanceled(true);
                if (vanillaHealthBarMode == FirstAidConfig.Client.VanillaHealthbarMode.HIGHLIGHT_CRITICAL_PATH && FirstAidConfig.SERVER.vanillaHealthCalculation.get() == FirstAidConfig.Server.VanillaHealthCalculationMode.AVERAGE_ALL) {
                    FirstaidIngameGui.renderHealth(Minecraft.getInstance().gui, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), event.getMatrixStack());
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent.Post event) {
        RenderGameOverlayEvent.ElementType type = event.getType();
        if (type == RenderGameOverlayEvent.ElementType.ALL || (type == RenderGameOverlayEvent.ElementType.TEXT && FirstAidConfig.CLIENT.overlayMode.get() != FirstAidConfig.Client.OverlayMode.OFF && FirstAidConfig.CLIENT.pos.get() == FirstAidConfig.Client.Position.BOTTOM_LEFT)) {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.player.isAlive()) return;
            mc.getProfiler().push("FirstAidOverlay");
            HUDHandler.INSTANCE.renderOverlay(event.getMatrixStack(), mc, event.getPartialTicks());
            mc.getProfiler().pop();
            mc.getProfiler().pop();
        }
    }

    @SubscribeEvent
    public static void onLivingRender(RenderLivingEvent.Post<PlayerEntity, PlayerModel<PlayerEntity>> event) {
        Entity entity = event.getEntity();
        if (entity instanceof PlayerEntity) {
            EntityRendererManager renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            if (renderDispatcher.shouldRenderHitBoxes()) {
                event.getMatrixStack().pushPose();
                //See PlayerRenderer.getRenderOffset
                if (entity.isCrouching()) {
                    event.getMatrixStack().translate(0D, 0.125D, 0D);
                }
                AxisAlignedBB aabb = entity.getBoundingBox();


                Collection<AABBAlignedBoundingBox> allBoxes = PlayerSizeHelper.getBoxes(entity).values();
                float r = 0.25F;
                float g = 1.0F;
                float b = 1.0F;

                for (AABBAlignedBoundingBox box : allBoxes) {
                    AxisAlignedBB bbox = box.createAABB(aabb);
                    WorldRenderer.renderLineBox(event.getMatrixStack(), event.getBuffers().getBuffer(RenderType.lines()), bbox.inflate(0.02D).move(-entity.getX(), -entity.getY(), -entity.getZ()), r, g, b, 1.0F);
                    r += 0.25F;
                    g += 0.5F;
                    b += 0.1F;

                    r %= 1.0F;
                    g %= 1.0F;
                    b %= 1.0F;
                }
                event.getMatrixStack().popPose();
            }
        }
    }

    private static ITextComponent makeArmorMsg(double value) {
        return new TranslationTextComponent("firstaid.specificarmor", FORMAT.format(value)).withStyle(TextFormatting.BLUE); //applyTextStyle
    }

    private static ITextComponent makeToughnessMsg(double value) {
        return new TranslationTextComponent("firstaid.specifictoughness", FORMAT.format(value)).withStyle(TextFormatting.BLUE); //applyTextStyle
    }

    private static <T> void replaceOrAppend(List<T> list, T search, T replace) {
        int index = list.indexOf(search);
        if (FirstAidConfig.CLIENT.armorTooltipMode.get() == FirstAidConfig.Client.TooltipMode.REPLACE && index >= 0) {
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
            event.getToolTip().add(new TranslationTextComponent("firstaid.tooltip.morphine", "3:30-4:30"));
            return;
        }
        if (FirstAidConfig.CLIENT.armorTooltipMode.get() != FirstAidConfig.Client.TooltipMode.NONE) {
            if (item instanceof ArmorItem) {
                ArmorItem armor = (ArmorItem) item;
                List<ITextComponent> tooltip = event.getToolTip();

                double normalArmor = ArmorUtils.getArmor(stack, armor.getSlot());
                double totalArmor = ArmorUtils.applyArmorModifier(armor.getSlot(), normalArmor);
                if (totalArmor > 0D) {
                    ITextComponent original = new TranslationTextComponent("attribute.modifier.plus.0", FORMAT.format(normalArmor), new TranslationTextComponent("attribute.name.generic.armor")).withStyle(TextFormatting.BLUE);
                    replaceOrAppend(tooltip, original, makeArmorMsg(totalArmor));
                }

                double normalToughness = ArmorUtils.getArmorToughness(stack, armor.getSlot());
                double totalToughness = ArmorUtils.applyToughnessModifier(armor.getSlot(), normalToughness);
                if (totalToughness > 0D) {
                    ITextComponent original = new TranslationTextComponent("attribute.modifier.plus.0", FORMAT.format(normalToughness), new TranslationTextComponent("attribute.name.generic.armor_toughness")).withStyle(TextFormatting.BLUE);
                    replaceOrAppend(tooltip, original, makeToughnessMsg(totalToughness));
                }
            }
        }

        AbstractPartHealer healer = FirstAidRegistryImpl.INSTANCE.getPartHealer(stack);
        if (healer != null && event.getPlayer() != null) {
            event.getToolTip().add(new TranslationTextComponent("firstaid.tooltip.healer", healer.maxHeal.getAsInt() / 2, StringUtils.formatTickDuration(healer.ticksPerHeal.getAsInt())));
        }
    }

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        FirstAid.isSynced = false;
        HUDHandler.INSTANCE.ticker = -1;
    }
}
