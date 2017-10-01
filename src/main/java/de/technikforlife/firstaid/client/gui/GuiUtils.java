package de.technikforlife.firstaid.client.gui;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.DamageablePart;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiUtils {
    public static final ResourceLocation GUI_LOCATION = new ResourceLocation(FirstAid.MODID, "textures/gui/show_wounds.png");

    public static void drawHealth(DamageablePart damageablePart, float xTranslation, float yTranslation, Gui gui, boolean secondLine) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(xTranslation, yTranslation, 0);
        int yTexture = damageablePart.canCauseDeath ? 45 : 0;
        float max = damageablePart.maxHealth + damageablePart.getAbsorption() + 0.4999F;
        float currentPlusAbsorption = damageablePart.currentHealth + 0.4999F + damageablePart.getAbsorption();
        renderIcon(max, Math.max(damageablePart.maxHealth, currentPlusAbsorption), yTexture, 16, 16, gui, secondLine);
        renderIcon(max, currentPlusAbsorption, yTexture, 160, 169, gui, secondLine);
        GlStateManager.translate(0, 0, 1);
        renderIcon(max, damageablePart.currentHealth + 0.4999F, yTexture, 52, 61, gui, secondLine);
        GlStateManager.popMatrix();
    }

    private static void renderIcon(float max, float available, int textureY, int textureX, int halfTextureX, Gui gui, boolean secondLine) {
        GlStateManager.pushMatrix();
        int maxHealth = Math.round(max);
        int availableHealth = Math.round(available);
        boolean lastOneHalf = availableHealth % 2 != 0;
        if (maxHealth > 16)
            throw new IllegalArgumentException("Can only draw up to 8 hearts!");
        int toDraw = secondLine ? Math.min(4, Math.round(availableHealth >> 1)) : (availableHealth >> 1) + (lastOneHalf ? 1 : 0);
        if (maxHealth > 8 && secondLine) {
            GlStateManager.translate(0, 5, 0);
            int toDrawSecond = (int) ((availableHealth - 8) / 2F) + (lastOneHalf ? 1 : 0);
            if (toDrawSecond > 0 && availableHealth > 8)
                renderTexturedModalRects(toDrawSecond, lastOneHalf, halfTextureX, textureX, textureY, gui);
            GlStateManager.translate(0, -10, 0);
        }
        renderTexturedModalRects(toDraw, lastOneHalf && (availableHealth < 8 || !secondLine), halfTextureX, textureX, textureY, gui);
        GlStateManager.popMatrix();
    }

    private static void renderTexturedModalRects(int toDraw, boolean lastOneHalf, int halfTextureX, int textureX, int textureY, Gui gui) {
        for (int i = 0; i < toDraw; i++) {
            boolean renderHalf = lastOneHalf && i + 1 == toDraw;
            int width = (renderHalf && halfTextureX == textureX) ? 5 : 9;
            gui.drawTexturedModalRect(i * 9, 0, renderHalf ? halfTextureX : textureX, textureY, width, 9);
        }
    }
}
