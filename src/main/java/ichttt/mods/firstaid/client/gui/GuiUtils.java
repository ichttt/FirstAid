package ichttt.mods.firstaid.client.gui;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.damagesystem.DamageablePart;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiUtils {
    public static final ResourceLocation GUI_LOCATION = new ResourceLocation(FirstAid.MODID, "textures/gui/show_wounds.png");

    public static void drawHealth(DamageablePart damageablePart, float xTranslation, float yTranslation, Gui gui, boolean secondLine) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(xTranslation, yTranslation, 0);
        int yTexture = damageablePart.canCauseDeath ? 45 : 0;
        int maxHealth = getMaxHearts(damageablePart.maxHealth);
        int maxExtraHealth = getMaxHearts(damageablePart.getAbsorption());
        int current = (int) Math.ceil(damageablePart.currentHealth);
        int absorption = (int) Math.ceil(damageablePart.getAbsorption());
        if (secondLine)
            secondLine = (maxHealth + maxExtraHealth) > 4;
        if (secondLine) {
            int maxHealth2 = 0;
            if (maxHealth > 4) {
                maxHealth2 = maxHealth - 4;
                maxHealth = 4;
            }

            int maxExtraHealth2 = Math.max(0, maxExtraHealth - (4 -maxHealth));
            maxExtraHealth -= maxExtraHealth2;

            int current2 = 0;
            if (current > 8) {
                current2 = current - 8;
                current = 8;
            }

            int absorption2 = absorption - maxExtraHealth * 2;
            absorption -= absorption2;

            GlStateManager.translate(0F, 5F, 0F);
            GlStateManager.pushMatrix();
            renderLine(yTexture, maxHealth2, maxExtraHealth2, current2, absorption2, gui);
            GlStateManager.popMatrix();
            GlStateManager.translate(0F, -10F, 0F);
        }
        renderLine(yTexture, maxHealth, maxExtraHealth, current, absorption, gui);

        GlStateManager.popMatrix();
    }

    private static void renderLine(int yTexture, int maxHealth, int maxExtraHearts, int current, int absorption, Gui gui) {
        GlStateManager.pushMatrix();
        renderMax(maxHealth, yTexture, gui);
        if (maxExtraHearts > 0) {
            if (maxHealth != 0)
                GlStateManager.translate(2, 0, 0);
            renderMax(maxExtraHearts, yTexture, gui);
        }
        GlStateManager.popMatrix();
        GlStateManager.translate(0, 0, 1);

        GlStateManager.pushMatrix();
        renderCurrentHealth(current, yTexture, gui);
        GlStateManager.popMatrix();

        if (absorption > 0) {
            int offset = maxHealth * 9 + (maxHealth == 0 ? 0 : 2);
            GlStateManager.translate(offset, 0, 0);
            renderAbsorption(absorption, yTexture, gui);
        }
    }

    public static int getMaxHearts(float value) {
        int maxCurrentHearths = (int) Math.ceil(value);
        if (maxCurrentHearths % 2 != 0)
            maxCurrentHearths ++;
        return maxCurrentHearths >> 1;
    }

    private static void renderMax(int max, int yTexture, Gui gui) {
        if (max > 8)
            throw new IllegalArgumentException("Can only draw up to 8 hearts!");
        renderTexturedModalRects(max, false, 16, 16, yTexture, gui);
    }

    private static void renderCurrentHealth(int current, int yTexture, Gui gui) {
        boolean renderLastHalf;
        int render;

        renderLastHalf = false;
        render = current >> 1;
        if (current % 2 != 0) {
            renderLastHalf = true;
            render++;
        }
        renderTexturedModalRects(render, renderLastHalf, 61, 52, yTexture, gui);
    }

    private static void renderAbsorption(int absorption, int yTexture, Gui gui) {
        boolean renderLastHalf = false;
        int render = absorption >> 1;
        if (absorption % 2 != 0) {
            renderLastHalf = true;
            render++;
        }

        if (render > 0)
            renderTexturedModalRects(render, renderLastHalf, 169, 160, yTexture, gui);
    }

    private static void renderTexturedModalRects(int toDraw, boolean lastOneHalf, int halfTextureX, int textureX, int textureY, Gui gui) {
        if (toDraw == 0)
            return;
        if (toDraw < 0)
            throw new IllegalArgumentException("Cannot draw negative amount of hearts");
        for (int i = 0; i < toDraw; i++) {
            boolean renderHalf = lastOneHalf && i + 1 == toDraw;
            gui.drawTexturedModalRect(0, 0, renderHalf ? halfTextureX : textureX, textureY, 9, 9);
            GlStateManager.translate(9F, 0F, 0F);
        }
    }
}
