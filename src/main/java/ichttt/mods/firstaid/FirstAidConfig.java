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

package ichttt.mods.firstaid;

import ichttt.mods.firstaid.common.config.ExtraConfig;
import net.minecraftforge.common.config.Config;

@SuppressWarnings("CanBeFinal")
@Config(modid = FirstAid.MODID, name = FirstAid.NAME)
@ExtraConfig
public class FirstAidConfig {

    @Config.Comment("Settings regarding the max health of the body's parts. 2 = 1 heart")
    @Config.LangKey("firstaid.config.damagesystem")
    @Config.RequiresWorldRestart
    @ExtraConfig.Sync
    public static final DamageSystem damageSystem = new DamageSystem();

    @Config.Comment("Settings regarding the health overlay when ingame")
    @Config.LangKey("firstaid.config.overlay")
    public static final Overlay overlay = new Overlay();

    @Config.Comment("Settings regarding the internal healing system")
    @Config.LangKey("firstaid.config.internalhealing")
    @Config.RequiresWorldRestart
    @ExtraConfig.Sync
    public static final InternalHealing internalHealing = new InternalHealing();

    @Config.Comment("Settings regarding external healing system(like vanilla potions or natural regeneration")
    @Config.LangKey("firstaid.config.externalhealing")
    @Config.RequiresWorldRestart
    @ExtraConfig.Sync
    public static final ExternalHealing externalHealing = new ExternalHealing();

    @Config.Comment("Enable/Disable specify debuffs on specific body parts")
    @Config.LangKey("firstaid.config.debuffs")
    @ExtraConfig.Advanced
    public static final Debuffs debuffs = new Debuffs();

    @Config.Comment("Set to true to enable the debuff sounds. Requieres enableDebuffs to be true")
    @Config.LangKey("firstaid.config.enablesoundsystem")
    @ExtraConfig.Advanced
    public static boolean enableSoundSystem = true;

    @Config.Comment("If true, max health is scaled to your hearts, and the config entries get multiplier to match the max health")
    @Config.LangKey("firstaid.config.scalemaxhealth")
    @Config.RequiresWorldRestart
    @ExtraConfig.Sync
    public static boolean scaleMaxHealth = false;

    @Config.Comment("If true, max health will be capped at 6 hearts and absorption at 2 hearts per limb. If false, the health cap will be much higher (64 hearts normal and 16 absorption)")
    @Config.LangKey("firstaid.config.capmaxhealth")
    @Config.RequiresWorldRestart
    @ExtraConfig.Sync
    public static boolean capMaxHealth = true;

    @Config.Comment("If true, all usages of setHealth from other mods will be captured. Should not cause any problems, but allow mods like scaling health bandages to apply")
    @Config.LangKey("firstaid.config.sethealth")
    @ExtraConfig.Advanced
    public static boolean watchSetHealth = true;

    @Config.Comment("If true, many damage distributions will be more realistic, but this will also cause them to be harder\nIf enabled, e.g. drowing will only damage your body instead of your body and head last")
    @Config.LangKey("firstaid.config.hardmode")
    @Config.RequiresMcRestart
    public static boolean hardMode = false;

    @Config.Comment("Specifies how the vanilla health is calculated. Affects the visual health bar, as well as the value other mods get when they query the player health.\n" +
            "AVERAGE_ALL simply takes all limbs and calculates the average of it.\n" +
            "AVERAGE_CRITICAL takes all critical limbs and calculates the average of it\n" +
            "MIN_CRITICAl takes the smallest health value of all critical limb\n" +
            "Does not have any effect if all critical limbs have been disabled.")
    @Config.RequiresWorldRestart
    @ExtraConfig.Sync
    public static VanillaHealthCalculationMode vanillaHealthCalculation = VanillaHealthCalculationMode.AVERAGE_ALL;

    public enum VanillaHealthCalculationMode {
        AVERAGE_ALL, AVERAGE_CRITICAL, MIN_CRITICAL, CRITICAL_50_PERCENT_OTHER_50_PERCENT
    }

    @Config.Comment("Only effects the fallback random distribution.\n" +
            "If enabled, the default random damage distribution will be changed to leave critical limbs at 1hp if possible.\n" +
            "When there is too much damage, the damage will still kill the player. Other distributions that defined are not affected by this.")
    public static boolean useFriendlyRandomDistribution = false;

    @Config.Comment("Enabled additional debug logs - May slow down the game and will increase log file size\nOnly enable for special purposes")
    @Config.LangKey("firstaid.config.debug")
    @Config.RequiresMcRestart
    @ExtraConfig.Advanced
    public static boolean debug = false;

    public static class InternalHealing {

        @Config.Comment("Settings for the bandage item")
        @Config.LangKey("firstaid.config.bandage")
        public final Entry bandage = new Entry(4, 18, 2500);

        @Config.Comment("Settings for the plaster item")
        @Config.LangKey("firstaid.config.plaster")
        public final Entry plaster = new Entry(2, 22, 3000);

        public static class Entry {
            @Config.Comment("The total heals this item does when applied. 1 heal = half a heart")
            @Config.LangKey("firstaid.config.totalheals")
            @Config.RangeInt(min = 1, max = Byte.MAX_VALUE)
            public int totalHeals;

            @Config.Comment("The time it takes for a single heal to trigger. Total time this item is active = this * totalHeals")
            @Config.LangKey("firstaid.config.secondsperheal")
            @Config.RangeInt(min = 1, max = Short.MAX_VALUE)
            public int secondsPerHeal;

            @Config.Comment("The time it takes in the GUI to apply the item in milliseconds")
            @Config.LangKey("firstaid.config.applytime")
            @Config.RangeInt(min = 0, max = 16000)
            public int applyTime;

            public Entry(int initialTotalHeals, int initialSecondsPerHeal, int initialApplyTime) {
                this.totalHeals = initialTotalHeals;
                this.secondsPerHeal = initialSecondsPerHeal;
                this.applyTime = initialApplyTime;
            }
        }
    }

    public static class ExternalHealing {

        @Config.Comment("Allow vanilla's natural regeneration. Requires \"allowOtherHealingItems\" to be true" + "\n**WARNING** This sets the gamerule \"naturalRegeneration\" for all of your worlds internally, so it persists even if you remove the mod")
        @Config.LangKey("firstaid.config.allownaturalregeneration")
        @Config.RequiresWorldRestart
        public boolean allowNaturalRegeneration = false;

        @Config.Comment("If false, healing potions and other healing items will have no effect")
        @Config.LangKey("firstaid.config.allowotherhealingitems")
        public boolean allowOtherHealingItems = true;

        @Config.Comment("Specifies how much percent of the max health should be restored when sleeping")
        @Config.LangKey("firstaid.config.sleephealpercentage")
        @Config.RangeDouble(min = 0D, max = 1D)
        public double sleepHealPercentage = 0.07D;

        @Config.Comment("The value external regen will be multiplied with. Has no effect if \"allowOtherHealingItems\" is disabled")
        @Config.LangKey("firstaid.config.otherregenmultiplier")
        @Config.RangeDouble(min = 0D, max = 20D)
        @ExtraConfig.Advanced
        public double otherRegenMultiplier = 0.75D;

        @Config.Comment("The value vanilla's natural regeneration will be multiplied with. Has no effect if \"allowNaturalRegeneration\" is disabled")
        @Config.LangKey("firstaid.config.naturalregenmultiplier")
        @Config.RangeDouble(min = 0D, max = 20D)
        @ExtraConfig.Advanced
        public double naturalRegenMultiplier = 0.5D;
    }

    public static class Overlay {

        public enum OverlayMode {
            OFF, NUMBERS, HEARTS, PLAYER_MODEL
        }

        public enum Position {
            TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
        }

        public enum TooltipMode {
            REPLACE, APPEND, NONE
        }

        public static class DisplayMode {
            @Config.Comment("Specifies how many ticks (20 ticks = 1 second) the overlay should be visible after health changed (healing/damaging)\n" +
                    "If set to -1, the HUD is always visible")
            @Config.LangKey("firstaid.config.visibledurationticks")
            @Config.RangeInt(min = -1, max = 600)
            public final int visibleDurationTicks;

            @Config.Comment("If set to true, the overlay will flash for a short moment if the health changed. Only affects PLAYER_MODEL overlay")
            @Config.LangKey("firstaid.config.flash")
            public final boolean flash;

            private DisplayMode(int visibleDurationTicks, boolean flash) {
                this.visibleDurationTicks = visibleDurationTicks;
                this.flash = flash;
            }
        }

        @Config.Comment("True if the main health bar should be rendered (Will be average health)")
        @Config.LangKey("firstaid.config.showvanillahealthbar")
        public boolean showVanillaHealthBar = false;

        @Config.Comment("Specifies when and how the HUD should be displayed")
        @Config.LangKey("firstaid.config.displaymode")
        public DisplayMode displayMode = new DisplayMode(-1, true);

        @Config.Comment("Specifies the type of the overlay HUD.")
        @Config.LangKey("firstaid.config.overlaymode")
        public OverlayMode overlayMode = OverlayMode.PLAYER_MODEL;

        @Config.Comment("The relative point of the overlay")
        @Config.LangKey("firstaid.config.position")
        public Position pos = Position.TOP_LEFT;

        @Config.Comment("The offset on the x axis")
        @Config.LangKey("firstaid.config.xoffset")
        @ExtraConfig.Advanced
        public int xOffset = 0;

        @Config.Comment("The offset on the y axis")
        @Config.LangKey("firstaid.config.yoffset")
        @ExtraConfig.Advanced
        public int yOffset = 1;

        @Config.Comment("Determines the transparency of the overlay. 200 = Maximum transparency, 0 = Fully opaque")
        @Config.LangKey("firstaid.config.alpha")
        @Config.RangeInt(min = 0, max = 200)
        @ExtraConfig.Advanced
        public int alpha = 50;

        @Config.Comment("Determines how first aid should display armor on item tooltips.\nREPLACE replaces the vanilla description with the one fitting first aid\nAPPEND will add the first aid values at the bottom\nNONE will show the old vanilla values. Be advised this is purly visual, interally, the first aid value will always be used")
        @ExtraConfig.Advanced
        public TooltipMode armorTooltipMode = TooltipMode.REPLACE;

        @Config.Comment("Disables the funny easter eggs on certain events")
        @ExtraConfig.Advanced
        public boolean enableEasterEggs = true;
    }

    public static class DamageSystem {

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthHead = 4;

        @Config.RequiresWorldRestart
        public boolean causeDeathHead = true;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthLeftArm = 4;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthLeftLeg = 4;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthLeftFoot = 4;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthBody = 6;

        @Config.RequiresWorldRestart
        public boolean causeDeathBody = true;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthRightArm = 4;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthRightLeg = 4;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthRightFoot = 4;
    }

    public static class Debuffs {
        public final Head head = new Head();
        public final Body body = new Body();
        public final Arms arms = new Arms();
        public final LegsAndFeet legsAndFeet = new LegsAndFeet();

        public static class Head {
            public boolean blindness = true;
            @Config.Comment("Holds the information how the debuff should be applied at different damage taken. Only use this if you know what you are doing.")
            public final ConditionOnHit blindnessConditions = new ConditionOnHit(new float[]{2F, 1F}, new int[]{8 * 20, 4 * 20});

            public boolean nausea = true;
            @Config.Comment("Holds the information how the debuff should be applied at different damage taken. Only use this if you know what you are doing.")
            public final ConditionOnHit nauseaConditions = new ConditionOnHit(new float[]{3F, 2F}, new int[]{16 * 20, 12 * 20});
        }

        public static class Body {
            public boolean nausea = true;
            @Config.Comment("Holds the information how the debuff should be applied at different damage taken. Only use this if you know what you are doing.")
            public final ConditionOnHit nauseaConditions = new ConditionOnHit(new float[]{4F, 2F}, new int[]{16 * 20, 8 * 20});

            public boolean weakness = true;
            @Config.Comment("Holds the information how the debuff should be applied at different health left. Only use this if you know what you are doing.")
            public final ConditionConstant weaknessConditions = new ConditionConstant(new float[]{0.25F, 0.50F}, new int[]{2, 1});
        }

        public static class Arms {
            public boolean mining_fatigue = true;
            @Config.Comment("Holds the information how the debuff should be applied at different health left. Only use this if you know what you are doing.")
            public final ConditionConstant miningFatigueConditions = new ConditionConstant(new float[]{0.25F, 0.50F, 0.75F}, new int[]{3, 2, 1});
        }

        public static class LegsAndFeet {
            public boolean slowness = true;
            @Config.Comment("Holds the information how the debuff should be applied at different health left. Only use this if you know what you are doing.")
            public final ConditionConstant slownessConditions = new ConditionConstant(new float[]{0.35F, 0.6F, 0.8F}, new int[]{3, 2, 1});
        }

        public static class ConditionOnHit {
            public ConditionOnHit(float[] defaultTaken, int[] defaultLength) {
                this.damageTaken = defaultTaken;
                this.debuffLength = defaultLength;
            }

            @Config.RequiresMcRestart
            @Config.Comment("How much damage the user must have taken for the debuff to apply at the mapped length. Must be sorted so the **highest** value comes first. 2 = 1 heart")
            @Config.RangeDouble(min = 0, max = 10)
            public float[] damageTaken;

            @Config.RequiresMcRestart
            @Config.Comment("How long the debuff should stay. If the first condition from the damageTaken config is met, the first value in this list will be taken")
            @Config.RangeInt(min = 0, max = Short.MAX_VALUE)
            public int[] debuffLength;
        }

        public static class ConditionConstant {
            public ConditionConstant(float[] defaultPercentage, int[] defaultStrength) {
                this.healthPercentageLeft = defaultPercentage;
                this.debuffStrength = defaultStrength;
            }

            @Config.RequiresMcRestart
            @Config.Comment("How much health the user must have left for the debuff to apply at the mapped length. Must be sorted so the **lowest** value comes first")
            @Config.RangeDouble(min = 0, max = 1)
            public float[] healthPercentageLeft;

            @Config.RequiresMcRestart
            @Config.Comment("How strong the potion effect should stay. If the first condition from the healthPercentageLeft config is met, the first value in this list will be taken")
            @Config.RangeInt(min = 0, max = Byte.MAX_VALUE)
            public int[] debuffStrength;
        }
    }
}
