package ichttt.mods.firstaid.client.gui;

import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.damagesystem.DamageablePart;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class HUDHandler {

    public static void renderOverlay(ScaledResolution scaledResolution) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!FirstAidConfig.overlay.showOverlay || mc.player == null || GuiApplyHealthItem.isOpen || mc.player.isCreative())
            return;
        PlayerDamageModel damageModel = PlayerDataManager.getDamageModel(mc.player);
        Objects.requireNonNull(damageModel);
        mc.getTextureManager().bindTexture(Gui.ICONS);
        Gui gui = mc.ingameGUI;
        int xOffset = FirstAidConfig.overlay.xOffset;
        int yOffset = FirstAidConfig.overlay.yOffset;
        switch (FirstAidConfig.overlay.position) {
            case 0:
                break;
            case 1:
                xOffset = scaledResolution.getScaledWidth() - xOffset - damageModel.getMaxRenderSize() - 60;
                break;
            case 2:
                yOffset = scaledResolution.getScaledHeight() - yOffset - 80;
                break;
            case 3:
                xOffset = scaledResolution.getScaledWidth() - xOffset - damageModel.getMaxRenderSize() - 60;
                yOffset = scaledResolution.getScaledHeight() - yOffset - 80;
                break;
            default:
                throw new RuntimeException("Invalid config option for position: " + FirstAidConfig.overlay.position);
        }
        if (mc.currentScreen instanceof GuiChat && FirstAidConfig.overlay.position == 2) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(xOffset, yOffset, 0F);
        for (DamageablePart part : damageModel) {
            mc.fontRenderer.drawString(I18n.format("gui." + part.part.toString().toLowerCase(Locale.ENGLISH)), 0, 0, 0xFFFFFF);
            mc.getTextureManager().bindTexture(Gui.ICONS);
            GuiUtils.drawHealth(part, 60, 0, gui, false);
            GlStateManager.translate(0, 10F, 0F);
        }
        GlStateManager.popMatrix();
    }
}
