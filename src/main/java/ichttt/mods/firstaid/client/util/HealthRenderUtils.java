/*
 * FirstAid
 * Copyright (C) 2017-2018
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

import com.google.common.collect.ImmutableMap;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.gui.FlashStateManager;
import ichttt.mods.firstaid.common.EventHandler;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import java.text.DecimalFormat;
import java.util.Objects;

public class HealthRenderUtils {
    public static final ResourceLocation GUI_LOCATION = new ResourceLocation(FirstAid.MODID, "textures/gui/show_wounds.png");
    public static final DecimalFormat TEXT_FORMAT = new DecimalFormat("0.0");
    private static final Object2IntOpenHashMap<EnumPlayerPart> prevHealth = new Object2IntOpenHashMap<>();
    private static final ImmutableMap<EnumPlayerPart, FlashStateManager> flashStates;

    static {
        ImmutableMap.Builder<EnumPlayerPart, FlashStateManager> builder = ImmutableMap.builder();
        for (EnumPlayerPart part : EnumPlayerPart.VALUES) {
            builder.put(part, new FlashStateManager());
        }
        flashStates = builder.build();
    }

    public static void drawHealthString(AbstractDamageablePart damageablePart, float xTranslation, float yTranslation, boolean allowSecondLine) {
        float absorption = damageablePart.getAbsorption();
        String text = TEXT_FORMAT.format(damageablePart.currentHealth) + "/" + damageablePart.getMaxHealth();
        if (absorption > 0) {
            String line2 = "+ " + TEXT_FORMAT.format(absorption);
            if (allowSecondLine) {
                Minecraft.getInstance().fontRenderer.drawStringWithShadow(line2, xTranslation + 3, yTranslation, 0xFFFFFF);
                GlStateManager.translatef(-5, 0, 0);
            } else {
                text += " " + line2;
            }
        }
        Minecraft.getInstance().fontRenderer.drawStringWithShadow(text, xTranslation, yTranslation, 0xFFFFFF);
    }

    private static void updatePrev(EnumPlayerPart part, int current, boolean playerDead) {
        if (!playerDead)
            prevHealth.put(part, current);
        else
            prevHealth.clear();
    }

    public static boolean healthChanged(AbstractDamageablePart damageablePart, boolean playerDead) {
        int current = (int) Math.ceil(damageablePart.currentHealth);
        if (prevHealth.containsKey(damageablePart.part)) {
            int prev = prevHealth.getInt(damageablePart.part);
            updatePrev(damageablePart.part, current, playerDead);
            return prev != current;
        }
        updatePrev(damageablePart.part, current, playerDead);
        return true;
    }

    public static void drawHealth(AbstractDamageablePart damageablePart, float xTranslation, float yTranslation, Gui gui, boolean allowSecondLine) {
        int maxHealth = getMaxHearts(damageablePart.getMaxHealth());
        int maxExtraHealth = getMaxHearts(damageablePart.getAbsorption());
        int current = (int) Math.ceil(damageablePart.currentHealth);
        FlashStateManager activeFlashState = Objects.requireNonNull(flashStates.get(damageablePart.part));

        if (prevHealth.containsKey(damageablePart.part)) {
            int prev = prevHealth.getInt(damageablePart.part);
            if (prev != current)
                activeFlashState.setActive(Util.milliTime());
        }

        if ((maxHealth + maxExtraHealth > 8 && allowSecondLine) || ((maxHealth + maxExtraHealth) > 12)) {
            drawHealthString(damageablePart, xTranslation, yTranslation, allowSecondLine);
            return;
        }

        int yTexture = damageablePart.canCauseDeath ? 45 : 0;
        int absorption = (int) Math.ceil(damageablePart.getAbsorption());
        boolean highlight = activeFlashState.update(Util.milliTime());

        Minecraft mc = Minecraft.getInstance();
        int regen = -1;
//        if (FirstAidConfig.externalHealing.allowOtherHealingItems && mc.player.isPotionActive(MobEffects.REGENERATION))
//            regen = (mc.ingameGUI.healthupdatecounter / 2) % 15; TODO
        boolean low = (current + absorption) < 1.25F;

        mc.getTextureManager().bindTexture(Gui.ICONS);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(xTranslation, yTranslation, 0);
        boolean drawSecondLine = allowSecondLine;
        if (allowSecondLine) drawSecondLine = (maxHealth + maxExtraHealth) > 4;

        if (drawSecondLine) {
            int maxHealth2 = 0;
            if (maxHealth > 4) {
                maxHealth2 = maxHealth - 4;
                maxHealth = 4;
            }

            int maxExtraHealth2 = Math.max(0, maxExtraHealth - (4 - maxHealth));
            maxExtraHealth -= maxExtraHealth2;

            int current2 = 0;
            if (current > 8) {
                current2 = current - 8;
                current = 8;
            }

            int absorption2 = absorption - maxExtraHealth * 2;
            absorption -= absorption2;

            GlStateManager.translatef(0F, 5F, 0F);
            GlStateManager.pushMatrix();
            renderLine(regen, low, yTexture, maxHealth2, maxExtraHealth2, current2, absorption2, gui, highlight);
            regen -= (maxHealth2 + maxExtraHealth);
            GlStateManager.popMatrix();
            GlStateManager.translatef(0F, -10F, 0F);
        }
        renderLine(regen, low, yTexture, maxHealth, maxExtraHealth, current, absorption, gui, highlight);

        GlStateManager.popMatrix();
    }

    private static void renderLine(int regen, boolean low, int yTexture, int maxHealth, int maxExtraHearts, int current, int absorption, Gui gui, boolean highlight) {
        GlStateManager.pushMatrix();
        Int2IntMap map = new Int2IntArrayMap();
        if (low) {
            for (int i = 0; i < (maxHealth + maxExtraHearts); i++)
                map.put(i, EventHandler.rand.nextInt(2));
        }

        renderMax(regen, map, maxHealth, yTexture, gui, highlight);
        if (maxExtraHearts > 0) { //for absorption
            if (maxHealth != 0) {
                GlStateManager.translatef(2 + 9 * maxHealth, 0, 0);
            }
            renderMax(regen - maxHealth, map, maxExtraHearts, yTexture, gui, false); //Do not highlight absorption
        }
        GlStateManager.popMatrix();
        GlStateManager.translatef(0, 0, 1);

        renderCurrentHealth(regen, map, current, yTexture, gui);

        if (absorption > 0) {
            int offset = maxHealth * 9 + (maxHealth == 0 ? 0 : 2);
            GlStateManager.translatef(offset, 0, 0);
            renderAbsorption(regen - maxHealth, map, absorption, yTexture, gui);
        }
    }

    public static int getMaxHearts(float value) {
        int maxCurrentHearts = (int) Math.ceil(value);
        if (maxCurrentHearts % 2 != 0)
            maxCurrentHearts++;
        return maxCurrentHearts >> 1;
    }

    private static void renderMax(int regen, Int2IntFunction function, int max, int yTexture, Gui gui, boolean highlight) {
        final int BACKGROUND = (highlight ? 25 : 16);
        renderTexturedModalRects(regen, function, max, false, BACKGROUND, BACKGROUND, yTexture, gui);
    }

    private static void renderCurrentHealth(int regen, Int2IntFunction function, int current, int yTexture, Gui gui) {
        boolean renderLastHalf;
        int render;

        renderLastHalf = false;
        render = current >> 1;
        if (current % 2 != 0) {
            renderLastHalf = true;
            render++;
        }
        renderTexturedModalRects(regen, function, render, renderLastHalf, 61, 52, yTexture, gui);
    }

    private static void renderAbsorption(int regen, Int2IntFunction function, int absorption, int yTexture, Gui gui) {
        boolean renderLastHalf = false;
        int render = absorption >> 1;
        if (absorption % 2 != 0) {
            renderLastHalf = true;
            render++;
        }

        if (render > 0) renderTexturedModalRects(regen, function, render, renderLastHalf, 169, 160, yTexture, gui);
    }

    private static void renderTexturedModalRects(int regen, Int2IntFunction function, int toDraw, boolean lastOneHalf, int halfTextureX, int textureX, int textureY, Gui gui) {
        if (toDraw == 0)
            return;
        if (toDraw < 0) throw new IllegalArgumentException("Cannot draw negative amount of icons " + toDraw);
        for (int i = 0; i < toDraw; i++) {
            boolean renderHalf = lastOneHalf && i + 1 == toDraw;
            gui.drawTexturedModalRect(9F * i, (i == regen ? -2 : 0) - function.get(i), renderHalf ? halfTextureX : textureX, textureY, 9, 9);
        }
    }
}
