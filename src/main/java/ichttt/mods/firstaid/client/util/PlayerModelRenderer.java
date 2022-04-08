/*
 * FirstAid
 * Copyright (C) 2017-2022
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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Vector3f;

import java.util.Random;

public class PlayerModelRenderer {
    private static final ResourceLocation HEALTH_RENDER_LOCATION = new ResourceLocation(FirstAid.MODID, "textures/gui/simple_health.png");
    private static final Random RANDOM = new Random();
    private static final int SIZE = 32;
    private static int angle = 0;
    private static boolean otherWay = false;
    private static int cooldown = 0;

    public static void renderPlayerHealth(PoseStack stack, AbstractPlayerDamageModel damageModel, boolean fourColors, GuiComponent gui, boolean flashState, float alpha, float partialTicks) {
        int yOffset = flashState ? 64 : 0;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1 - (alpha / 255));
        RenderSystem.setShaderTexture(0, HEALTH_RENDER_LOCATION);
        if (FirstAidConfig.CLIENT.enableEasterEggs.get() && (EventCalendar.isAFDay() || EventCalendar.isHalloween())) {
            float angle = PlayerModelRenderer.angle;
            if (cooldown == 0) {
                angle += ((otherWay ? -partialTicks : partialTicks) * 2);
            }
            if (FirstAidConfig.CLIENT.pos.get() == FirstAidConfig.Client.Position.BOTTOM_LEFT || FirstAidConfig.CLIENT.pos.get() == FirstAidConfig.Client.Position.TOP_LEFT)
                stack.translate(angle * 1.5F, 0, 0);
            else
                stack.translate(angle * 0.5F, 0, 0);
            stack.mulPose(Vector3f.ZP.rotationDegrees(angle));
        }

        if (yOffset != 0)
            stack.translate(0, -yOffset, 0);

        drawPart(stack, gui, fourColors, damageModel.HEAD, 8, yOffset + 0, 16, 16);
        drawPart(stack, gui, fourColors, damageModel.BODY, 8, yOffset + 16, 16, 24);
        drawPart(stack, gui, fourColors, damageModel.LEFT_ARM, 0, yOffset + 16, 8, 24);
        drawPart(stack, gui, fourColors, damageModel.RIGHT_ARM, 24, yOffset + 16, 8, 24);
        drawPart(stack, gui, fourColors, damageModel.LEFT_LEG, 8, yOffset + 40, 8, 16);
        drawPart(stack, gui, fourColors, damageModel.RIGHT_LEG, 16, yOffset + 40, 8, 16);
        drawPart(stack, gui, fourColors, damageModel.LEFT_FOOT, 8, yOffset + 56, 8, 8);
        drawPart(stack, gui, fourColors, damageModel.RIGHT_FOOT, 16, yOffset + 56, 8, 8);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    private static void drawPart(PoseStack stack, GuiComponent gui, boolean fourColors, AbstractDamageablePart part, int texX, int texY, int sizeX, int sizeY) {
        int rawTexX = texX;
        texX += SIZE * getState(part, fourColors);
        gui.blit(stack, rawTexX, texY, texX, texY, sizeX, sizeY);
    }

    private static int getState(AbstractDamageablePart part, boolean fourColors) {
        if (part.currentHealth <= 0.001F) {
            return 5;
        }
        int maxHealth = part.getMaxHealth();
        if (Math.abs(part.currentHealth - maxHealth) < 0.001F) {
            return 0;
        }
        float healthPercentage = part.currentHealth / maxHealth;
        if (healthPercentage >= 1 || healthPercentage <= 0)
            throw new RuntimeException(String.format("Calculated invalid health for part %s with current health %s and max health %d. Got value %s", part.part, part.currentHealth, maxHealth, healthPercentage));
        if (!fourColors && healthPercentage > 0.75F) {
            return 1;
        }
        if (healthPercentage > 0.5F) {
            return 2;
        }
        if (!fourColors && healthPercentage > 0.25F) {
            return 3;
        }
        return 4;
    }

    public static void tickFun() {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        angle += otherWay ? -2 : 2;
        if (angle >= 90 || angle <= 0) {
            otherWay = !otherWay;
            if (!otherWay) {
                // Halloween is spooky, so make it more rare
                int multiplier = EventCalendar.isHalloween() ? 10 : 1;
                cooldown = (200 + RANDOM.nextInt(400)) * multiplier;
            } else {
                int multiplier = EventCalendar.isHalloween() ? 2 : 1;
                cooldown = (30 + RANDOM.nextInt(60)) * multiplier;
            }
        }
    }
}
