/*
 * FirstAid
 * Copyright (C) 2017-2019
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
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class PlayerModelRenderer {
    private static final ResourceLocation HEALTH_RENDER_LOCATION = new ResourceLocation(FirstAid.MODID, "textures/gui/simple_health.png");
    private static final int SIZE = 64;

    public static void renderPlayerHealth(AbstractPlayerDamageModel damageModel, AbstractGui gui, float alpha) {
        GlStateManager.enableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.color4f(1F, 1F, 1F, 1 - (alpha / 255));
        Minecraft.getInstance().getTextureManager().bindTexture(HEALTH_RENDER_LOCATION);
        GlStateManager.scalef(0.5F, 0.5F, 0.5F);
        drawPart(gui, damageModel.HEAD, 16, 0, 32, 32);
        drawPart(gui, damageModel.BODY, 16, 32, 32, 48);
        drawPart(gui, damageModel.LEFT_ARM, 0, 32, 16, 48);
        drawPart(gui, damageModel.RIGHT_ARM, 48, 32, 16, 48);
        drawPart(gui, damageModel.LEFT_LEG, 16, 80, 16, 32);
        drawPart(gui, damageModel.RIGHT_LEG, 32, 80, 16, 32);
        drawPart(gui, damageModel.LEFT_FOOT, 16, 112, 16, 16);
        drawPart(gui, damageModel.RIGHT_FOOT, 32, 112, 16, 16);

        GlStateManager.color4f(1F, 1F, 1F, 1F);
    }

    private static void drawPart(AbstractGui gui, AbstractDamageablePart part, int texX, int texY, int sizeX, int sizeY) {
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
}
