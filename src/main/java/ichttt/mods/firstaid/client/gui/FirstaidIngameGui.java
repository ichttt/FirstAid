/*
 * FirstAid
 * Copyright (C) 2017-2023
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

package ichttt.mods.firstaid.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public class FirstaidIngameGui {

    // Copy of ForgeGui#renderHealth, modified to fit being called from an event listener and to support different textures for different parts of the texture
    public static void renderHealth(ForgeGui gui, int width, int height, PoseStack stack) {
        // Firstaid: No pre event, we get called from this
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getProfiler().push("health");
        // Firstaid: calculate criticalDamage
        AbstractPlayerDamageModel damageModel = CommonUtils.getOptionalDamageModel(minecraft.player).orElse(null);
        int criticalHalfHearts;
        if (damageModel != null) {
            float criticalHealth = Float.MAX_VALUE;
            for (AbstractDamageablePart part : damageModel) {
                if (part.canCauseDeath) {
                    criticalHealth = Math.min(criticalHealth, part.currentHealth);
                }
            }
            criticalHealth = (criticalHealth / (float) damageModel.getCurrentMaxHealth()) * minecraft.player.getMaxHealth();
            criticalHalfHearts = Mth.ceil(criticalHealth);
        } else {
            criticalHalfHearts = 0;
        }
        RenderSystem.enableBlend();

        Player player = (Player) minecraft.getCameraEntity();
        int health = Mth.ceil(player.getHealth());
        boolean highlight = gui.healthBlinkTime > (long) gui.tickCount && (gui.healthBlinkTime - (long) gui.tickCount) / 3L % 2L == 1L;

        if (health < gui.lastHealth && player.invulnerableTime > 0)
        {
            gui.lastHealthTime = Util.getMillis();
            gui.healthBlinkTime = (long)(gui.tickCount + 20);
        }
        else if (health > gui.lastHealth && player.invulnerableTime > 0)
        {
            gui.lastHealthTime = Util.getMillis();
            gui.healthBlinkTime = (long)(gui.tickCount + 10);
        }

        if (Util.getMillis() - gui.lastHealthTime > 1000L)
        {
            gui.lastHealth = health;
            gui.displayHealth = health;
            gui.lastHealthTime = Util.getMillis();
        }

        gui.lastHealth = health;
        int healthLast = gui.displayHealth;

        AttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = Math.max((float) attrMaxHealth.getValue(), Math.max(healthLast, health));
        int absorb = Mth.ceil(player.getAbsorptionAmount());

        int healthRows = Mth.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        gui.random.setSeed((long)(gui.tickCount * 312871));

        int left = width / 2 - 91;
        int top = height - gui.leftHeight;
        gui.leftHeight += (healthRows * rowHeight);
        if (rowHeight != 10) gui.leftHeight += 10 - rowHeight;

        int regen = -1;
        if (player.hasEffect(MobEffects.REGENERATION))
        {
            regen = gui.tickCount % Mth.ceil(healthMax + 5.0F);
        }

        final int BACKGROUND = (highlight ? 25 : 16);
        int MARGIN = 16;
        if (player.hasEffect(MobEffects.POISON))      MARGIN += 36;
        else if (player.hasEffect(MobEffects.WITHER)) MARGIN += 72;
        float absorbRemaining = absorb;

        for (int i = Mth.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i)
        {
            boolean thisHalfCritical = (i * 2) + 1 == criticalHalfHearts;
            final int TOP =  9 * (i * 2 < (criticalHalfHearts) && !thisHalfCritical ? 5 : 0);
            //int b0 = (highlight ? 1 : 0);
            int row = Mth.ceil((float)(i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) y += gui.random.nextInt(2);
            if (i == regen) y -= 2;

            GuiComponent.blit(stack, x, y, BACKGROUND, TOP, 9, 9);

            if (highlight)
            {
                if (thisHalfCritical) {
                    stack.pushPose();
                    stack.translate(0.0F, 0.0F, 1000.0F);
                    GuiComponent.blit(stack, x, y, MARGIN + 63, 9 * 5, 9, 9);
                    stack.popPose();
                }
                if (i * 2 + 1 < healthLast)
                    GuiComponent.blit(stack, x + (thisHalfCritical ? 5 : 0), y, MARGIN + 54 + (thisHalfCritical ? 5 : 0), TOP, 9 - (thisHalfCritical ? 5 : 0), 9); //6
                else if (i * 2 + 1 == healthLast)
                    GuiComponent.blit(stack, x, y, MARGIN + 63, TOP, 9, 9); //7
            }

            if (absorbRemaining > 0.0F)
            {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F)
                {
                    GuiComponent.blit(stack, x, y, MARGIN + 153, TOP, 9, 9); //17
                    absorbRemaining -= 1.0F;
                }
                else
                {
                    GuiComponent.blit(stack, x, y, MARGIN + 144, TOP, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
            }
            else
            {
                if (thisHalfCritical) {
                    stack.pushPose();
                    stack.translate(0.0F, 0.0F, 10.0F);
                    GuiComponent.blit(stack, x, y, MARGIN + 45, 9 * 5, 9, 9);
                    stack.popPose();
                }
                if (i * 2 + 1 < health)
                    GuiComponent.blit(stack, x + (thisHalfCritical ? 5 : 0), y, MARGIN + 36 + (thisHalfCritical ? 5 : 0), TOP, 9 - (thisHalfCritical ? 5 : 0), 9); //4
                else if (i * 2 + 1 == health && !thisHalfCritical)
                    GuiComponent.blit(stack, x, y, MARGIN + 45, TOP, 9, 9); //5
            }
        }

        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
    }
}
