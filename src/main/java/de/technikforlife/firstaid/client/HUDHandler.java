package de.technikforlife.firstaid.client;

import de.technikforlife.firstaid.damagesystem.DamageablePart;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

@SideOnly(Side.CLIENT)
public class HUDHandler {

    public static void renderOverlay(ScaledResolution scaledResolution) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || GuiApplyHealthItem.isOpen)
            return;
        PlayerDamageModel damageModel = mc.player.getCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null);
        Objects.requireNonNull(damageModel);
        mc.getTextureManager().bindTexture(Gui.ICONS);
        Gui gui = mc.ingameGUI;
        GlStateManager.pushMatrix();
//        GlStateManager.scale(0.75F, 0.75F, 0F);
        GlStateManager.translate(0F, scaledResolution.getScaledHeight() - 90, 0F);
        for (DamageablePart part : damageModel) {
            GlStateManager.translate(0, 10F, 0F);
            mc.fontRenderer.drawString(part.part.toString(), 0, 0, 0xFFFFFF);
            mc.getTextureManager().bindTexture(Gui.ICONS);
            RenderUtils.drawHealth(part, 60, 0, gui, false);
        }
        GlStateManager.popMatrix();
    }
}
