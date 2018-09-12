package ichttt.mods.firstaid.client.util;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlayerModelRenderer {
    private static final ResourceLocation HEALTH_RENDER_LOCATION = new ResourceLocation(FirstAid.MODID, "textures/gui/simple_health.png");
    private static final int SIZE = 64;

    public static void renderPlayerHealth(AbstractPlayerDamageModel damageModel, Gui gui, float alpha) {
        GlStateManager.enableAlpha();
        GlStateManager.color(1F, 1F, 1F, 1 - (alpha / 255));
        Minecraft.getMinecraft().getTextureManager().bindTexture(HEALTH_RENDER_LOCATION);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        drawPart(gui, damageModel.HEAD, 16, 0, 32, 32);
        drawPart(gui, damageModel.BODY, 16, 32, 32, 48);
        drawPart(gui, damageModel.LEFT_ARM, 0, 32, 16, 48);
        drawPart(gui, damageModel.RIGHT_ARM, 48, 32, 16, 48);
        drawPart(gui, damageModel.LEFT_LEG, 16, 80, 16, 32);
        drawPart(gui, damageModel.RIGHT_LEG, 32, 80, 16, 32);
        drawPart(gui, damageModel.LEFT_FOOT, 16, 112, 16, 16);
        drawPart(gui, damageModel.RIGHT_FOOT, 32, 112, 16, 16);

        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private static void drawPart(Gui gui, AbstractDamageablePart part, int texX, int texY, int sizeX, int sizeY) {
        int rawTexX = texX;
        int maxHealth = part.getMaxHealth();
        if (part.currentHealth <= 0.001) {
            texX += SIZE * 3;
        }
        else if (Math.abs(part.currentHealth - maxHealth) > 0.001) {
            float healthPercentage = part.currentHealth / maxHealth;
            if (healthPercentage >= 1 || healthPercentage <= 0)
                throw new RuntimeException(String.format("Calculated invalid health for part %s with current health %s and max health %d. Got value %s", part.part, part.currentHealth, maxHealth, healthPercentage));
            texX += SIZE * (healthPercentage > 0.5 ? 1 : 2);
        }
//        Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
        gui.drawTexturedModalRect(rawTexX, texY, texX, texY, sizeX, sizeY);
//        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(Strings.repeat('-', 200), 20, 20, 0xFFFFFF);
    }
}
