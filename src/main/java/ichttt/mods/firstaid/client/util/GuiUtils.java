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
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class GuiUtils {
    public static final ResourceLocation GUI_LOCATION = new ResourceLocation(FirstAid.MODID, "textures/gui/show_wounds.png");
    private static final Object2IntOpenHashMap<EnumPlayerPart> prevHealth = new Object2IntOpenHashMap<>();
    private static final ImmutableMap<EnumPlayerPart, FlashStateManager> flashStates;
    public static final Map<EnumPlayerPart, Int2IntArrayMap> lowHealthList = new HashMap<>();

    static {
        ImmutableMap.Builder<EnumPlayerPart, FlashStateManager> builder = ImmutableMap.builder();
        for (EnumPlayerPart part : EnumPlayerPart.VALUES) {
            builder.put(part, new FlashStateManager());
        }
        flashStates = builder.build();
    }

    public static void drawHealth(AbstractDamageablePart damageablePart, float xTranslation, float yTranslation, Gui gui, boolean secondLine, boolean playerDead) {
        int yTexture = damageablePart.canCauseDeath ? 45 : 0;
        int maxHealth = getMaxHearts(damageablePart.getMaxHealth());
        int maxExtraHealth = getMaxHearts(damageablePart.getAbsorption());
        int current = (int) Math.ceil(damageablePart.currentHealth);
        int absorption = (int) Math.ceil(damageablePart.getAbsorption());
        FlashStateManager activeFlashState = Objects.requireNonNull(flashStates.get(damageablePart.part));
        if (prevHealth.containsKey(damageablePart.part)) {
            int prev = prevHealth.getInt(damageablePart.part);
            if (prev != current)
                activeFlashState.setActive(Minecraft.getSystemTime());
        }
        if (!playerDead)
            prevHealth.put(damageablePart.part, current);
        else
            prevHealth.clear();
        boolean highlight = activeFlashState.update(Minecraft.getSystemTime());

        Minecraft mc = Minecraft.getMinecraft();
        int regen = -1;
        if (FirstAid.activeHealingConfig != null && FirstAid.activeHealingConfig.allowOtherHealingItems && mc.player.isPotionActive(MobEffects.REGENERATION))
            regen = (mc.ingameGUI.updateCounter / 2) % 15;
        boolean low = (current + absorption) < 1.25F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(xTranslation, yTranslation, 0);
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
            renderLine(regen, low, yTexture, maxHealth2, maxExtraHealth2, current2, absorption2, gui, highlight);
            regen -= (maxHealth2 + maxExtraHealth);
            GlStateManager.popMatrix();
            GlStateManager.translate(0F, -10F, 0F);
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
                GlStateManager.translate(2 + 9 * maxHealth, 0, 0);
            }
            renderMax(regen - maxHealth, map, maxExtraHearts, yTexture, gui, false); //Do not highlight absorption
        }
        GlStateManager.popMatrix();
        GlStateManager.translate(0, 0, 1);

        renderCurrentHealth(regen, map, current, yTexture, gui);

        if (absorption > 0) {
            int offset = maxHealth * 9 + (maxHealth == 0 ? 0 : 2);
            GlStateManager.translate(offset, 0, 0);
            renderAbsorption(regen - maxHealth, map, absorption, yTexture, gui);
        }
    }

    public static int getMaxHearts(float value) {
        int maxCurrentHearths = (int) Math.ceil(value);
        if (maxCurrentHearths % 2 != 0)
            maxCurrentHearths ++;
        return maxCurrentHearths >> 1;
    }

    private static void renderMax(int regen, Int2IntFunction function, int max, int yTexture, Gui gui, boolean highlight) {
        if (max > 8)
            throw new IllegalArgumentException("Can only draw up to 8 hearts!");
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
        if (toDraw < 0)
            throw new IllegalArgumentException("Cannot draw negative amount of hearts " + toDraw);
        for (int i = 0; i < toDraw; i++) {
            boolean renderHalf = lastOneHalf && i + 1 == toDraw;
            gui.drawTexturedModalRect(9F * i, (i == regen ? -2 : 0) - function.get(i), renderHalf ? halfTextureX : textureX, textureY, 9, 9);
        }
    }
}
