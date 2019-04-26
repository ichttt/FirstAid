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

package ichttt.mods.firstaid;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FirstAidConfig {

    static final ForgeConfigSpec serverSpec;
    static final ForgeConfigSpec generalSpec;
    public static final FirstAidConfig.Server SERVER;
    public static final FirstAidConfig.General GENERAL;

    static {
        final Pair<FirstAidConfig.Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(FirstAidConfig.Server::new);
        serverSpec = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    static {
        final Pair<FirstAidConfig.General, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(FirstAidConfig.General::new);
        generalSpec = specPair.getRight();
        GENERAL = specPair.getLeft();
    }

    public static class Server {

        Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Server to Client synced configuration settings").push("Damage System");

            maxHealthHead = healthEntry(builder, "Head", 4);
            maxHealthLeftArm = healthEntry(builder, "Left Arm", 4);
            maxHealthLeftLeg = healthEntry(builder, "Left Leg", 4);
            maxHealthLeftFoot = healthEntry(builder, "Left Foot", 4);
            maxHealthBody = healthEntry(builder, "Body", 6);
            maxHealthRightArm = healthEntry(builder, "Right Arm", 4);
            maxHealthRightLeg = healthEntry(builder, "Right Leg", 4);
            maxHealthRightFoot = healthEntry(builder, "Right Foot", 4);
            causeDeathHead = builder
                    .comment("True if the head can cause death if health drops to 0")
                    .translation("firstaid.config.causedeath.head")
                    .define("causeDeathHead", true);
            causeDeathBody = builder
                    .comment("True if the body can cause death if health drops to 0")
                    .translation("firstaid.config.causedeath.body")
                    .define("causeDeathBody", true);

            builder.pop().push("Internal Healing");

            bandage = new IEEntry(builder, "bandage", 4, 18, 2500);
            plaster = new IEEntry(builder, "plaster", 2, 22, 3000);

            builder.pop().push("External Healing");

            allowNaturalRegeneration = builder
                    .comment("Allow vanilla's natural regeneration. Requires \"allowOtherHealingItems\" to be true", "**WARNING** This sets the gamerule \"naturalRegeneration\" for all of your worlds internally, so it persists even if you remove the mod")
                    .translation("firstaid.config.allownaturalregeneration")
                    .worldRestart()
                    .define("allowNaturalRegeneration", false);

            allowOtherHealingItems = builder
                    .comment("If false, healing potions and other healing items will have no effect")
                    .translation("firstaid.config.allowotherhealingitems")
                    .define("allowOtherHealingItems", true);

            sleepHealPercentage = builder
                    .comment("Specifies how much percent of the max health should be restored when sleeping")
                    .translation("firstaid.config.sleephealpercentage")
                    .defineInRange("sleepHealPercentage", 0.07D, 0D, 1D);
            otherRegenMultiplier = builder
                    .comment("The value external regen will be multiplied with. Has no effect if \"allowOtherHealingItems\" is disabled")
                    .translation("firstaid.config.otherregenmultiplier")
                    .defineInRange("otherRegenMultiplier", 0.75D, 0D, 20D);
            naturalRegenMultiplier = builder
                    .comment("The value vanilla's natural regeneration will be multiplied with. Has no effect if \"allowNaturalRegeneration\" is disabled")
                    .translation("firstaid.config.naturalregenmultiplier")
                    .defineInRange("naturalRegenMultiplier", 0.5D, 0D, 20D);

            builder.pop();
        }

        public final ForgeConfigSpec.IntValue maxHealthHead;
        public final ForgeConfigSpec.BooleanValue causeDeathHead;
        public final ForgeConfigSpec.IntValue maxHealthLeftArm;
        public final ForgeConfigSpec.IntValue maxHealthLeftLeg;
        public final ForgeConfigSpec.IntValue maxHealthLeftFoot;
        public final ForgeConfigSpec.IntValue maxHealthBody;
        public final ForgeConfigSpec.BooleanValue causeDeathBody;
        public final ForgeConfigSpec.IntValue maxHealthRightArm;
        public final ForgeConfigSpec.IntValue maxHealthRightLeg;
        public final ForgeConfigSpec.IntValue maxHealthRightFoot;

        public final IEEntry bandage;
        public final IEEntry plaster;

        public final ForgeConfigSpec.BooleanValue allowNaturalRegeneration;
        public final ForgeConfigSpec.BooleanValue allowOtherHealingItems;
        public final ForgeConfigSpec.DoubleValue sleepHealPercentage;
        public final ForgeConfigSpec.DoubleValue otherRegenMultiplier;
        public final ForgeConfigSpec.DoubleValue naturalRegenMultiplier;


        private static ForgeConfigSpec.IntValue healthEntry(ForgeConfigSpec.Builder builder, String name, int defaultVal) {
            String noSpaceName = name.replace(' ', '_');
            return builder.comment("Max health of the " + name).translation("firstaid.config.maxhealth." + noSpaceName.toLowerCase(Locale.ENGLISH)).defineInRange("maxHealth" + noSpaceName, defaultVal, 2, 12);
        }

        public static class IEEntry {

            public final ForgeConfigSpec.IntValue totalHeals;
            public final ForgeConfigSpec.IntValue secondsPerHeal;
            public final ForgeConfigSpec.IntValue applyTime;

            IEEntry(ForgeConfigSpec.Builder builder, String name, int initialTotalHeals, int initialSecondsPerHeal, int initialApplyTime) {
                builder.push(name);
                totalHeals = builder
                        .comment("The total heals this item does when applied. 1 heal = half a heart")
                        .translation("firstaid.config.totalheals")
                        .defineInRange("totalsHeals", initialTotalHeals, 1, Byte.MAX_VALUE);
                secondsPerHeal = builder
                        .comment("The time it takes for a single heal to trigger. Total time this item is active = this * totalHeals")
                        .translation("firstaid.config.secondsperheal")
                        .defineInRange("secondsPerHeal", initialSecondsPerHeal, 1, Short.MAX_VALUE);
                applyTime = builder
                        .comment("The time it takes in the GUI to apply the item in milliseconds")
                        .translation("firstaid.config.applytime")
                        .defineInRange("applyTime", initialApplyTime, 0, 16000);
                builder.pop();
            }
        }
    }

    public static class General {

        public General(ForgeConfigSpec.Builder builder) {
            builder.comment("Server only configuration settings").push("Debuffs");
            head = new General.Head(builder);
            body = new General.Body(builder);
            arms = new General.Arms(builder);
            legsAndFeet = new General.LegsAndFeet(builder);
            builder.pop();
        }

        public final Head head;
        public final Body body;
        public final Arms arms;
        public final LegsAndFeet legsAndFeet;

        public static class Head {

            Head(ForgeConfigSpec.Builder builder) {
                builder.push("Head");
                this.blindnessConditions = new ConditionOnHit(builder, "blindness", Arrays.asList(2F, 1F), Arrays.asList(8 * 20, 4 * 20));
                this.nauseaConditions = new ConditionOnHit(builder, "nausea", Arrays.asList(3F, 2F), Arrays.asList(16 * 20, 12 * 20));
                builder.pop();
            }

            public final ConditionOnHit blindnessConditions;
            public final ConditionOnHit nauseaConditions;
        }

        public static class Body {

            Body(ForgeConfigSpec.Builder builder) {
                builder.push("Body");
                this.nauseaConditions = new ConditionOnHit(builder, "nausea", Arrays.asList(4F, 2F), Arrays.asList(16 * 20, 8 * 20));
                this.weaknessConditions = new ConditionConstant(builder, "weakness", Arrays.asList(0.25F, 0.50F), Arrays.asList(2, 1));
                builder.pop();
            }
            public final ConditionOnHit nauseaConditions;
            public final ConditionConstant weaknessConditions;
        }

        public static class Arms {

            Arms(ForgeConfigSpec.Builder builder) {
                builder.push("Arms");
                this.miningFatigueConditions = new ConditionConstant(builder, "miningFatigue", Arrays.asList(0.25F, 0.50F, 0.75F), Arrays.asList(3, 2, 1));
                builder.pop();
            }
            public final ConditionConstant miningFatigueConditions;
        }

        public static class LegsAndFeet {

            LegsAndFeet(ForgeConfigSpec.Builder builder) {
                builder.push("Legs and Feet");
                this.slownessConditions = new ConditionConstant(builder, "slowness", Arrays.asList(0.35F, 0.6F, 0.8F), Arrays.asList(3, 2, 1));
                builder.pop();
            }
            public final ConditionConstant slownessConditions;
        }

        public static class ConditionOnHit {
            public ConditionOnHit(ForgeConfigSpec.Builder builder, String name, List<Float> defaultTaken, List<Integer> defaultLength) {
                builder.push(name);
                this.enabled = builder
                        .comment("Enables/Disables this debuff")
                        .translation("firstaid.config.debuff.enabled")
                        .define("enabled", true);
                this.damageTaken = builder
                        .comment("How much damage the user must have taken for the debuff to apply at the mapped length. Must be sorted so the **highest** value comes first. 2 = 1 heart")
                        .translation("firstaid.config.debuff.damagetaken")
                        .defineList("damageTaken", defaultTaken, o -> {
                            try {
                                float val = Float.parseFloat(o.toString());
                                return val >= 0F && val <= 10F;
                            } catch (NumberFormatException ignored) {}
                            FirstAid.LOGGER.warn("Invalid entry " + o.toString() + " for damageTaken at " + name);
                            return false;
                        });
                this.debuffLength = builder
                        .comment("How long the debuff should stay. If the first condition from the damageTaken config is met, the first value in this list will be taken")
                        .translation("firstaid.config.debuff.debufflength")
                        .defineList("debuffLength", defaultLength, o -> {
                            try {
                                float val = Float.parseFloat(o.toString());
                                return val >= 0F && val <= Short.MAX_VALUE;
                            } catch (NumberFormatException ignored) {}
                            FirstAid.LOGGER.warn("Invalid entry " + o.toString() + " for debuffLength at " + name);
                            return false;
                        });
                builder.pop();
            }
            public final ForgeConfigSpec.BooleanValue enabled;
            public final ForgeConfigSpec.ConfigValue<List<? extends Float>> damageTaken;
            public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> debuffLength;
        }

        public static class ConditionConstant {
            public ConditionConstant(ForgeConfigSpec.Builder builder, String name, List<Float> defaultPercentage, List<Integer> defaultStrength) {
                builder.push(name);
                this.enabled = builder
                        .comment("Enables/Disables this debuff")
                        .translation("firstaid.config.debuff.enabled")
                        .define("enabled", true);
                this.healthPercentageLeft = builder
                        .comment("How much health the user must have left for the debuff to apply at the mapped length. Must be sorted so the **lowest** value comes first")
                        .translation("firstaid.config.debuff.healthpercentageleft")
                        .defineList("healthPercentageLeft", defaultPercentage, o -> {
                            try {
                                float val = Float.parseFloat(o.toString());
                                return val >= 0F && val <= 1F;
                            } catch (NumberFormatException ignored) {}
                            FirstAid.LOGGER.warn("Invalid entry " + o.toString() + " for healthPercentageLeft at " + name);
                            return false;
                        });
                this.debuffStrength = builder
                        .comment("How strong the potion effect should stay. If the first condition from the healthPercentageLeft config is met, the first value in this list will be taken")
                        .translation("firstaid.config.debuff.debuffstrength")
                        .defineList("debuffStrength", defaultStrength, o -> {
                            try {
                                float val = Float.parseFloat(o.toString());
                                return val >= 0F && val <= Short.MAX_VALUE;
                            } catch (NumberFormatException ignored) {}
                            FirstAid.LOGGER.warn("Invalid entry " + o.toString() + " for debuffStrength at " + name);
                            return false;
                        });
                builder.pop();
            }

            public final ForgeConfigSpec.BooleanValue enabled;
            public final ForgeConfigSpec.ConfigValue<List<? extends Float>> healthPercentageLeft;
            public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> debuffStrength;
        }
    }

    //    @Config.Comment("Settings regarding the health overlay when ingame")
//    @Config.LangKey("firstaid.config.overlay")
    public static final Overlay overlay = new Overlay(); //TODO client

    //    @Config.Comment("Set to true to enable the debuff sounds. Requieres enableDebuffs to be true")
//    @Config.LangKey("firstaid.config.enablesoundsystem")
//    @ExtraConfig.Advanced
    public static boolean enableSoundSystem = true; //TODO client

    //    @Config.Comment("If true, max health is scaled to your hearts, and the config entries get multiplier to match the max health")
//    @Config.LangKey("firstaid.config.scalemaxhealth")
//    @Config.RequiresWorldRestart
//    @ExtraConfig.Sync
    public static boolean scaleMaxHealth = false;

    //    @Config.Comment("If true, max health will be capped at 6 hearts and absorption at 2 hearts per limb. If false, the health cap will be much higher (64 hearts normal and 16 absorption)")
//    @Config.LangKey("firstaid.config.capmaxhealth")
//    @Config.RequiresWorldRestart
//    @ExtraConfig.Sync
    public static boolean capMaxHealth = true;

//    @Config.Comment("If true, all usages of setHealth from other mods will be captured. Should not cause any problems, but allow mods like scaling health bandages to apply")
//    @Config.LangKey("firstaid.config.sethealth")
//    @ExtraConfig.Advanced
    public static boolean watchSetHealth = true; //TODO server as well

    public static class Overlay {

        public enum OverlayMode {
            OFF, NUMBERS, HEARTS, PLAYER_MODEL
        }

        public enum Position {
            TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
        }

        //        @Config.Comment("True if the main health bar should be rendered (Will be average health)")
//        @Config.LangKey("firstaid.config.showvanillahealthbar")
        public boolean showVanillaHealthBar = false;

        //        @Config.Comment("If true the overlay will automatically be hidden while health isn't changing. It will be shown when connecting and any health changes")
//        @Config.LangKey("firstaid.config.hideonnochange")
        public boolean hideOnNoChange = false;

        public OverlayMode overlayMode = OverlayMode.PLAYER_MODEL;

        //        @Config.Comment("The relative point of the overlay")
//        @Config.LangKey("firstaid.config.position")
        public Position pos = Position.TOP_LEFT;

        //        @Config.Comment("The offset on the x axis")
//        @Config.LangKey("firstaid.config.xoffset")
//        @ExtraConfig.Advanced
        public int xOffset = 0;

        //        @Config.Comment("The offset on the y axis")
//        @Config.LangKey("firstaid.config.yoffset")
//        @ExtraConfig.Advanced
        public int yOffset = 1;

        //        @Config.Comment("Determines the transparency of the overlay. 200 = Maximum transparency, 0 = Fully opaque")
//        @Config.LangKey("firstaid.config.alpha")
//        @Config.RangeInt(min = 0, max = 200)
//        @ExtraConfig.Advanced
        public int alpha = 50;
    }
}
