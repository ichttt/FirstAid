package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class HUDHandler {
    private static final Map<EnumPlayerPart, String> TRANSLATION_MAP = new HashMap<>();
    private static final int FADE_TIME = 30;
    private static int maxLength;
    public static int ticker = -1;

    public static void rebuildTranslationTable() {
        FirstAid.logger.debug("Building GUI translation table");
        TRANSLATION_MAP.clear();
        maxLength = 0;
        for (EnumPlayerPart part : EnumPlayerPart.VALUES) {
            String translated = I18n.format("gui." + part.toString().toLowerCase(Locale.ENGLISH));
            maxLength = Math.max(maxLength, Minecraft.getMinecraft().fontRenderer.getStringWidth(translated));
            TRANSLATION_MAP.put(part, translated);
        }
    }

    public static int getMaxLength() {
        return maxLength;
    }

    public static void renderOverlay(ScaledResolution scaledResolution, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!FirstAidConfig.overlay.showOverlay || mc.player == null || GuiHealthScreen.isOpen || !CommonUtils.isSurvivalOrAdventure(mc.player))
            return;

        AbstractPlayerDamageModel damageModel = Objects.requireNonNull(PlayerDataManager.getDamageModel(mc.player));
        if (damageModel.isTemp) //Wait until we receive the remote model
            return;

        if (FirstAidConfig.overlay.onlyShowWhenDamaged) {
            for (AbstractDamageablePart damageablePart : damageModel) {
                if (HealthRenderUtils.healthChanged(damageablePart)) {
                    ticker = Math.max(ticker, 100);
                    break;
                }
            }
            if (ticker <= 0)
                return;
        }

        mc.getTextureManager().bindTexture(Gui.ICONS);
        Gui gui = mc.ingameGUI;
        int xOffset = FirstAidConfig.overlay.xOffset;
        int yOffset = FirstAidConfig.overlay.yOffset;
        switch (FirstAidConfig.overlay.position) {
            case 0:
                break;
            case 1:
                xOffset = scaledResolution.getScaledWidth() - xOffset - damageModel.getMaxRenderSize() - (maxLength);
                break;
            case 2:
                yOffset = scaledResolution.getScaledHeight() - yOffset - 80;
                break;
            case 3:
                xOffset = scaledResolution.getScaledWidth() - xOffset - damageModel.getMaxRenderSize() - (maxLength);
                yOffset = scaledResolution.getScaledHeight() - yOffset - 80;
                break;
            default:
                throw new RuntimeException("Invalid config option for position: " + FirstAidConfig.overlay.position);
        }

        if (mc.currentScreen instanceof GuiChat && FirstAidConfig.overlay.position == 2)
            return;
        if (mc.gameSettings.showDebugInfo && FirstAidConfig.overlay.position == 0)
            return;

        boolean enableAlphaBlend = FirstAidConfig.overlay.onlyShowWhenDamaged && ticker < FADE_TIME;
        int alpha = enableAlphaBlend ? MathHelper.clamp((int)((FADE_TIME - ticker) * 255.0F / (float) FADE_TIME), 0, 250) : 0;

        GlStateManager.pushMatrix();
        GlStateManager.scale(FirstAidConfig.overlay.hudScale, FirstAidConfig.overlay.hudScale, 1);
        GlStateManager.translate(xOffset, yOffset, 0F);
        if (enableAlphaBlend) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        boolean playerDead = damageModel.isDead(mc.player);

        int xTranslation = maxLength;
        for (AbstractDamageablePart part : damageModel) {
            mc.fontRenderer.drawStringWithShadow(TRANSLATION_MAP.get(part.part), 0, 0, 0xFFFFFF - (alpha << 24 & -0xFFFFFF));
            if (FirstAidConfig.overlay.displayHealthAsNumber) {
                HealthRenderUtils.drawHealthString(part, xTranslation, 0, false);
            } else {
                HealthRenderUtils.drawHealth(part, xTranslation, 0, gui, false, playerDead);
            }
            GlStateManager.translate(0, 10F, 0F);
        }
        if (enableAlphaBlend)
            GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
