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

package ichttt.mods.firstaid.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.gui.FlashStateManager;
import ichttt.mods.firstaid.common.EventHandler;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.Objects;

public class HealthRenderUtils {
    public static final ResourceLocation GUI_LOCATION = new ResourceLocation(FirstAid.MODID, "textures/gui/show_wounds.png");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png"); 
    public static final DecimalFormat TEXT_FORMAT = new DecimalFormat("0.0");
    private static final Object2IntOpenHashMap<EnumPlayerPart> prevHealth = new Object2IntOpenHashMap<>();
    private static final EnumMap<EnumPlayerPart, FlashStateManager> flashStates = new EnumMap<>(EnumPlayerPart.class);
    

    static {
        for (EnumPlayerPart part : EnumPlayerPart.VALUES) {
            flashStates.put(part, new FlashStateManager());
        }
    }

    public static void drawHealthString(GuiGraphics stack, AbstractDamageablePart damageablePart, float xTranslation, float yTranslation, boolean allowSecondLine) {
        float absorption = damageablePart.getAbsorption();
        String text = TEXT_FORMAT.format(damageablePart.currentHealth) + "/" + damageablePart.getMaxHealth();
        if (absorption > 0) {
            String line2 = "+ " + TEXT_FORMAT.format(absorption);
            if (allowSecondLine) {
                Minecraft.getInstance().font.drawInBatch(line2, xTranslation, yTranslation, 0, true, null, null, DisplayMode.NORMAL, 0, 0);
                //drawShadow(stack, line2, xTranslation, yTranslation + 5, 0xFFFFFF);
                yTranslation -= 5;
            } else {
                text += " " + line2;
            }
        }
        Minecraft.getInstance().font.drawInBatch(text, xTranslation, yTranslation, 0xFFFFFF, true, null, null, DisplayMode.NORMAL, 0, 0);
        //.drawShadow(stack, text, xTranslation, yTranslation, 0xFFFFFF);
    }

    private static void updatePrev(EnumPlayerPart part, int current, boolean playerDead) {
        if (!playerDead)
            prevHealth.put(part, current);
        else
            prevHealth.clear();
    }

    public static boolean healthChanged(AbstractDamageablePart damageablePart, boolean playerDead) {
        int current = (int) Math.ceil(damageablePart.currentHealth);
        FlashStateManager activeFlashState = Objects.requireNonNull(flashStates.get(damageablePart.part));
        if (prevHealth.containsKey(damageablePart.part)) {
            int prev = prevHealth.getInt(damageablePart.part);
            updatePrev(damageablePart.part, current, playerDead);
            if (prev != current) {
                activeFlashState.setActive(Util.getMillis());
                return true;
            }
            return false;
        }
        activeFlashState.setActive(Util.getMillis());
        updatePrev(damageablePart.part, current, playerDead);
        return true;
    }

    public static boolean drawAsString(AbstractDamageablePart damageablePart, boolean allowSecondLine) {
        int maxHealth = getMaxHearts(damageablePart.getMaxHealth());
        int maxExtraHealth = getMaxHearts(damageablePart.getAbsorption());
        return (maxHealth + maxExtraHealth > 8 && allowSecondLine) || ((maxHealth + maxExtraHealth) > 12);
    }

    public static void drawHealth(GuiGraphics gui, AbstractDamageablePart damageablePart, float xTranslation, float yTranslation, boolean allowSecondLine) {
        int maxHealth = getMaxHearts(damageablePart.getMaxHealth());
        int maxExtraHealth = getMaxHearts(damageablePart.getAbsorption());
        int current = (int) Math.ceil(damageablePart.currentHealth);
        FlashStateManager activeFlashState = Objects.requireNonNull(flashStates.get(damageablePart.part));

        if (drawAsString(damageablePart, allowSecondLine)) {
            drawHealthString(gui, damageablePart, xTranslation, yTranslation, allowSecondLine);
            return;
        }

        int yTexture = damageablePart.canCauseDeath ? 45 : 0;
        int absorption = (int) Math.ceil(damageablePart.getAbsorption());
        boolean highlight = activeFlashState.update(Util.getMillis());

        Minecraft mc = Minecraft.getInstance();
        int regen = -1;
        if (FirstAidConfig.SERVER.allowOtherHealingItems.get() && mc.player.hasEffect(MobEffects.REGENERATION))
            regen = (int) ((mc.gui.getGuiTicks() / 2) % 15);
        boolean low = (current + absorption) < 1.25F;
        
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        gui.pose().pushPose();
        gui.pose().translate(xTranslation, yTranslation, 0);
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

            gui.pose().translate(0F, 5F, 0F);
            gui.pose().pushPose();
            renderLine(gui, regen, low, yTexture, maxHealth2, maxExtraHealth2, current2, absorption2, highlight);
            regen -= (maxHealth2 + maxExtraHealth);
            gui.pose().popPose();
            gui.pose().translate(0F, -10F, 0F);
        }
        renderLine(gui, regen, low, yTexture, maxHealth, maxExtraHealth, current, absorption, highlight);

        gui.pose().popPose();
    }

    private static void renderLine(GuiGraphics stack, int regen, boolean low, int yTexture, int maxHealth, int maxExtraHearts, int current, int absorption, boolean highlight) {
        stack.pose().pushPose();
        Int2IntMap map = new Int2IntArrayMap();
        if (low) {
            for (int i = 0; i < (maxHealth + maxExtraHearts); i++)
                map.put(i, EventHandler.RAND.nextInt(2));
        }

        renderMax(stack, regen, map, maxHealth, yTexture, highlight);
        if (maxExtraHearts > 0) { //for absorption
            if (maxHealth != 0) {
                stack.pose().translate(2 + 9 * maxHealth, 0, 0);
            }
            renderMax(stack, regen - maxHealth, map, maxExtraHearts, yTexture, false); //Do not highlight absorption
        }
        stack.pose().popPose();
        stack.pose().translate(0, 0, 1);

        renderCurrentHealth(stack, regen, map, current, yTexture);

        if (absorption > 0) {
            int offset = maxHealth * 9 + (maxHealth == 0 ? 0 : 2);
            stack.pose().translate(offset, 0, 0);
            renderAbsorption(stack, regen - maxHealth, map, absorption, yTexture);
        }
    }

    public static int getMaxHearts(float value) {
        int maxCurrentHearts = (int) Math.ceil(value);
        if (maxCurrentHearts % 2 != 0)
            maxCurrentHearts++;
        return maxCurrentHearts >> 1;
    }

    private static void renderMax(GuiGraphics stack, int regen, Int2IntFunction function, int max, int yTexture, boolean highlight) {
        final int BACKGROUND = (highlight ? 25 : 16);
        renderTexturedModalRects(stack, regen, function, max, false, BACKGROUND, BACKGROUND, yTexture);
    }

    private static void renderCurrentHealth(GuiGraphics stack, int regen, Int2IntFunction function, int current, int yTexture) {
        boolean renderLastHalf;
        int render;

        renderLastHalf = false;
        render = current >> 1;
        if (current % 2 != 0) {
            renderLastHalf = true;
            render++;
        }
        renderTexturedModalRects(stack, regen, function, render, renderLastHalf, 61, 52, yTexture);
    }

    private static void renderAbsorption(GuiGraphics stack, int regen, Int2IntFunction function, int absorption, int yTexture) {
        boolean renderLastHalf = false;
        int render = absorption >> 1;
        if (absorption % 2 != 0) {
            renderLastHalf = true;
            render++;
        }

        if (render > 0) renderTexturedModalRects(stack, regen, function, render, renderLastHalf, 169, 160, yTexture);
    }

    private static void renderTexturedModalRects(GuiGraphics stack, int regen, Int2IntFunction function, int toDraw, boolean lastOneHalf, int halfTextureX, int textureX, int textureY) {
        if (toDraw == 0)
            return;
        if (toDraw < 0) throw new IllegalArgumentException("Cannot draw negative amount of icons " + toDraw);
        for (int i = 0; i < toDraw; i++) {
            boolean renderHalf = lastOneHalf && i + 1 == toDraw;
            
        stack.blit(GUI_ICONS_LOCATION, (int) (9F * i), (i == regen ? -2 : 0) - function.get(i), renderHalf ? halfTextureX : textureX, textureY, 9, 9);
        }
    }
}
