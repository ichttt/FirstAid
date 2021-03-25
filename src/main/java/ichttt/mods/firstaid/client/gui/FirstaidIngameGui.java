/*
 * FirstAid
 * Copyright (C) 2017-2021
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

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HEALTH;

@SideOnly(Side.CLIENT)
public class FirstaidIngameGui {
    private static final Field eventParentField;

    static {
        Field f;
        try {
            f = GuiIngameForge.class.getDeclaredField("eventParent");
            f.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            FirstAid.LOGGER.error("Failed to get eventParent", e);
            f = null;
        }
        eventParentField = f;
    }

    public static void renderHealth(GuiIngame gui, int width, int height) {
        // Firstaid: No pre event, we get called from this
        Minecraft mc = Minecraft.getMinecraft();
        mc.profiler.startSection("health");
        // Firstaid: calculate criticalDamage
        AbstractPlayerDamageModel damageModel = mc.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
        int criticalHalfHearts;
        if (damageModel != null) {
            float criticalHealth = Float.MAX_VALUE;
            for (AbstractDamageablePart part : damageModel) {
                if (part.canCauseDeath) {
                    criticalHealth = Math.min(criticalHealth, part.currentHealth);
                }
            }
            criticalHealth = (criticalHealth / (float) damageModel.getCurrentMaxHealth()) * mc.player.getMaxHealth();
            criticalHalfHearts = MathHelper.ceil(criticalHealth);
        } else {
            criticalHalfHearts = 0;
        }
        GlStateManager.enableBlend();

        EntityPlayer player = (EntityPlayer)mc.getRenderViewEntity();
        int health = MathHelper.ceil(player.getHealth());
        boolean highlight = gui.healthUpdateCounter > (long)gui.updateCounter && (gui.healthUpdateCounter - (long)gui.updateCounter) / 3L %2L == 1L;

        if (health < gui.playerHealth && player.hurtResistantTime > 0)
        {
            gui.lastSystemTime = Minecraft.getSystemTime();
            gui.healthUpdateCounter = (long)(gui.updateCounter + 20);
        }
        else if (health > gui.playerHealth && player.hurtResistantTime > 0)
        {
            gui.lastSystemTime = Minecraft.getSystemTime();
            gui.healthUpdateCounter = (long)(gui.updateCounter + 10);
        }

        if (Minecraft.getSystemTime() - gui.lastSystemTime > 1000L)
        {
            gui.playerHealth = health;
            gui.lastPlayerHealth = health;
            gui.lastSystemTime = Minecraft.getSystemTime();
        }

        gui.playerHealth = health;
        int healthLast = gui.lastPlayerHealth;

        IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        float healthMax = (float)attrMaxHealth.getAttributeValue();
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());

        int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        gui.rand.setSeed((long)(gui.updateCounter * 312871));

        int left = width / 2 - 91;
        int top = height - GuiIngameForge.left_height;
        GuiIngameForge.left_height += (healthRows * rowHeight);
        if (rowHeight != 10) GuiIngameForge.left_height += 10 - rowHeight;

        int regen = -1;
        if (player.isPotionActive(MobEffects.REGENERATION))
        {
            regen = gui.updateCounter % 25;
        }

        final int BACKGROUND = (highlight ? 25 : 16);
        int MARGIN = 16;
        if (player.isPotionActive(MobEffects.POISON))      MARGIN += 36;
        else if (player.isPotionActive(MobEffects.WITHER)) MARGIN += 72;
        float absorbRemaining = absorb;

        for (int i = MathHelper.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i)
        {
            boolean thisHalfCritical = (i * 2) + 1 == criticalHalfHearts;
            final int TOP =  9 * (i * 2 < (criticalHalfHearts) && !thisHalfCritical ? 5 : 0);
            //int b0 = (highlight ? 1 : 0);
            int row = MathHelper.ceil((float)(i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) y += gui.rand.nextInt(2);
            if (i == regen) y -= 2;

            gui.drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);

            if (highlight)
            {
                if (thisHalfCritical) {
                    float oldBlitOffset = gui.zLevel;
                    gui.zLevel += 1000;
                    gui.drawTexturedModalRect(x, y, MARGIN + 63, 9 * 5, 9, 9);
                    gui.zLevel = oldBlitOffset;
                }
                if (i * 2 + 1 < healthLast)
                    gui.drawTexturedModalRect(x + (thisHalfCritical ? 5 : 0), y, MARGIN + 54 + (thisHalfCritical ? 5 : 0), TOP, 9 - (thisHalfCritical ? 5 : 0), 9); //6
                else if (i * 2 + 1 == healthLast)
                    gui.drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9); //7
            }

            if (absorbRemaining > 0.0F)
            {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F)
                {
                    gui.drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9); //17
                    absorbRemaining -= 1.0F;
                }
                else
                {
                    gui.drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
            }
            else
            {
                if (thisHalfCritical) {
                    float oldBlitOffset = gui.zLevel;
                    gui.zLevel += 1000;
                    gui.drawTexturedModalRect(x, y, MARGIN + 45, 9 * 5, 9, 9);
                    gui.zLevel = oldBlitOffset;
                }
                if (i * 2 + 1 < health)
                    gui.drawTexturedModalRect(x + (thisHalfCritical ? 5 : 0), y, MARGIN + 36 + (thisHalfCritical ? 5 : 0), TOP, 9 - (thisHalfCritical ? 5 : 0), 9); //4
                else if (i * 2 + 1 == health && !thisHalfCritical)
                    gui.drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9); //5
            }
        }

        GlStateManager.disableBlend();
        mc.profiler.endSection();
        if (eventParentField != null) {
            RenderGameOverlayEvent event;
            try {
                 event = (RenderGameOverlayEvent) eventParentField.get(gui);
            } catch (IllegalAccessException e) {
                FirstAid.LOGGER.error("Failed to access eventParentField", e);
                event = null;
            }
            if (event != null) {
                MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(event, HEALTH));
            }
        }
    }
}