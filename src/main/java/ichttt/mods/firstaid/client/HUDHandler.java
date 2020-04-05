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

import com.mojang.blaze3d.platform.GlStateManager;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.gui.FlashStateManager;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.client.util.PlayerModelRenderer;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class HUDHandler implements ISelectiveResourceReloadListener {
    public static final HUDHandler INSTANCE = new HUDHandler();
    private static final int FADE_TIME = 30;
    private final Map<EnumPlayerPart, String> TRANSLATION_MAP = new EnumMap<>(EnumPlayerPart.class);
    private final FlashStateManager flashStateManager = new FlashStateManager();
    private int maxLength;
    public int ticker = -1;

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, @Nonnull Predicate<IResourceType> resourcePredicate) {
        if (!resourcePredicate.test(VanillaResourceType.LANGUAGES)) return;
        buildTranslationTable();
    }

    private synchronized void buildTranslationTable() {
        FirstAid.LOGGER.debug("Building GUI translation table");
        TRANSLATION_MAP.clear();
        maxLength = 0;
        for (EnumPlayerPart part : EnumPlayerPart.VALUES) {
            String translated = I18n.format("firstaid.gui." + part.toString().toLowerCase(Locale.ENGLISH));
            maxLength = Math.max(maxLength, Minecraft.getInstance().fontRenderer.getStringWidth(translated));
            TRANSLATION_MAP.put(part, translated);
        }
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void renderOverlay(ScaledResolution scaledResolution, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.profiler.startSection("prepare");
        if (mc.player == null)
            return;

        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(mc.player);
        if (!FirstAid.isSynced) //Wait until we receive the remote model
            return;

        if (TRANSLATION_MAP.isEmpty()) buildTranslationTable(); //just to make sure

        int visibleTicks = FirstAidConfig.overlay.displayMode.visibleDurationTicks;
        if (visibleTicks != -1) visibleTicks += FADE_TIME;
        boolean playerDead = damageModel.isDead(mc.player);
        for (AbstractDamageablePart damageablePart : damageModel) {
            if (HealthRenderUtils.healthChanged(damageablePart, playerDead)) { //Always call healthChanged, it affects the GUI as well
                if (visibleTicks != -1)
                    ticker = Math.max(ticker, visibleTicks);
                if (FirstAidConfig.overlay.displayMode.flash) {
                    flashStateManager.setActive(Minecraft.getSystemTime());
                }
            }
        }

        if (FirstAidConfig.overlay.overlayMode == FirstAidConfig.Overlay.OverlayMode.OFF || (GuiHealthScreen.isOpen && FirstAidConfig.overlay.overlayMode != FirstAidConfig.Overlay.OverlayMode.PLAYER_MODEL) || !CommonUtils.isSurvivalOrAdventure(mc.player))
            return;

        if (visibleTicks != -1 && ticker < 0)
            return;

        mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
        AbstractGui gui = mc.ingameGUI;
        int xOffset = FirstAidConfig.CLIENT.xOffset.get();
        int yOffset = FirstAidConfig.CLIENT.yOffset.get();
        boolean playerModel = FirstAidConfig.CLIENT.overlayMode.get() == FirstAidConfig.Client.OverlayMode.PLAYER_MODEL;
        switch (FirstAidConfig.CLIENT.pos.get()) {
            case TOP_LEFT:
                if (playerModel)
                    xOffset += 1;
                break;
            case TOP_RIGHT:
                xOffset = scaledResolution.getScaledWidth() - xOffset - (playerModel ? 34 : damageModel.getMaxRenderSize() + (maxLength));
                break;
            case BOTTOM_LEFT:
                if (playerModel)
                    xOffset += 1;
                yOffset = mc.mainWindow.getScaledHeight() - yOffset - (playerModel ? 66 : 80);
                break;
            case BOTTOM_RIGHT:
                xOffset = scaledResolution.getScaledWidth() - xOffset - (playerModel ? 34 : damageModel.getMaxRenderSize() + (maxLength));
                yOffset = scaledResolution.getScaledHeight() - yOffset - (playerModel ? 62 : 80);
                break;
            default:
                throw new RuntimeException("Invalid config option for position: " + FirstAidConfig.CLIENT.pos.get());
        }

        if (mc.currentScreen instanceof ChatScreen && FirstAidConfig.CLIENT.pos.get() == FirstAidConfig.Client.Position.BOTTOM_LEFT)
            return;
        if (mc.gameSettings.showDebugInfo && FirstAidConfig.CLIENT.pos.get() == FirstAidConfig.Client.Position.TOP_LEFT)
            return;

        boolean enableAlphaBlend = visibleTicks != -1 && ticker < FADE_TIME;
        int alpha = enableAlphaBlend ? MathHelper.clamp((int)((FADE_TIME - ticker) * 255.0F / (float) FADE_TIME), FirstAidConfig.overlay.alpha, 250) : FirstAidConfig.overlay.alpha;

        GlStateManager.pushMatrix();
        GlStateManager.translatef(xOffset, yOffset, 0F);
        if (enableAlphaBlend) {
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        mc.profiler.endStartSection("render");
        if (FirstAidConfig.overlay.overlayMode == FirstAidConfig.Overlay.OverlayMode.PLAYER_MODEL) {
            PlayerModelRenderer.renderPlayerHealth(damageModel, gui, flashStateManager.update(Minecraft.getSystemTime()), alpha, partialTicks);
        } else {
            int xTranslation = maxLength;
            for (AbstractDamageablePart part : damageModel) {
                mc.fontRenderer.drawStringWithShadow(TRANSLATION_MAP.get(part.part), 0, 0, 0xFFFFFF - (alpha << 24 & -0xFFFFFF));
                if (FirstAidConfig.CLIENT.overlayMode.get() == FirstAidConfig.Client.OverlayMode.NUMBERS) {
                    HealthRenderUtils.drawHealthString(part, xTranslation, 0, false);
                } else {
                    HealthRenderUtils.drawHealth(part, xTranslation, 0, gui, false);
                }
                GlStateManager.translatef(0, 10F, 0F);
            }
        }
        mc.getProfiler().endStartSection("cleanup");
        if (enableAlphaBlend)
            GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
