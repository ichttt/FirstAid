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

package ichttt.mods.firstaid.client.util;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
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
    private static int angle = 0;
    private static boolean otherWay = false;

    public static void renderPlayerHealth(AbstractPlayerDamageModel damageModel, Gui gui, boolean flashState, float alpha, float partialTicks) {
        int yOffset = flashState ? 128 : 0;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1F, 1F, 1F, 1 - (alpha / 255));
        Minecraft.getMinecraft().getTextureManager().bindTexture(HEALTH_RENDER_LOCATION);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        if (FirstAidConfig.overlay.enableEasterEggs && (EventCalendar.isAFDay() || EventCalendar.isHalloween())) {
            float angle = PlayerModelRenderer.angle + ((otherWay ? -partialTicks : partialTicks) * 2);
            if (FirstAidConfig.overlay.pos == FirstAidConfig.Overlay.Position.BOTTOM_LEFT || FirstAidConfig.overlay.pos == FirstAidConfig.Overlay.Position.TOP_LEFT)
                GlStateManager.translate(angle * 1.5F, 0, 0);
            else
                GlStateManager.translate(angle * 0.5F, 0, 0);
            GlStateManager.rotate(angle, 0, 0, 1);
        }

        if (yOffset != 0)
            GlStateManager.translate(0, -yOffset, 0);

        drawPart(gui, damageModel.HEAD, 16, yOffset + 0, 32, 32);
        drawPart(gui, damageModel.BODY, 16, yOffset + 32, 32, 48);
        drawPart(gui, damageModel.LEFT_ARM, 0, yOffset + 32, 16, 48);
        drawPart(gui, damageModel.RIGHT_ARM, 48, yOffset + 32, 16, 48);
        drawPart(gui, damageModel.LEFT_LEG, 16, yOffset + 80, 16, 32);
        drawPart(gui, damageModel.RIGHT_LEG, 32, yOffset + 80, 16, 32);
        drawPart(gui, damageModel.LEFT_FOOT, 16, yOffset + 112, 16, 16);
        drawPart(gui, damageModel.RIGHT_FOOT, 32, yOffset + 112, 16, 16);

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.popMatrix();
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
        gui.drawTexturedModalRect(rawTexX, texY, texX, texY, sizeX, sizeY);
    }

    public static void tickFun() {
        angle += otherWay ? -2 : 2;
        if (angle >= 90 || angle <= 0) {
            otherWay = !otherWay;
        }
    }
}
