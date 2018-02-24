package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import net.minecraftforge.common.config.Config;

@SuppressWarnings("CanBeFinal")
@Config(modid = FirstAid.MODID, name = FirstAid.NAME)
@ExtraConfig
public class FirstAidConfig {

    @Config.Comment("Settings regarding the max health of the body's parts. 2 = 1 heart")
    @Config.LangKey("firstaid.config.damagesystem")
    @Config.RequiresWorldRestart
    public static final DamageSystem damageSystem = new DamageSystem();

    @Config.Comment("Settings regarding the health overlay when ingame")
    @Config.LangKey("firstaid.config.overlay")
    public static final Overlay overlay = new Overlay();

    @Config.Comment("Settings regarding external healing system(like vanilla potions or natural regeneration")
    @Config.LangKey("firstaid.config.externalhealing")
    public static final ExternalHealing externalHealing = new ExternalHealing();

    @Config.Comment("Enable/Disable specify debuffs on specific body parts")
    @Config.LangKey("firstaid.config.debuffs")
    @ExtraConfig.Advanced(warningKey = "firstaid.config.debuffwarn")
    public static final Debuffs debuffs = new Debuffs();

    @Config.Comment("Set to true to enable the debuff sounds. Requieres enableDebuffs to be true")
    @Config.LangKey("firstaid.config.enablesoundsystem")
    @ExtraConfig.Advanced
    public static boolean enableSoundSystem = true;

    @Config.Comment("If true, max health is scaled to your hearts, and the config entries get multiplier to match the max health")
    @Config.LangKey("firstaid.config.scalemaxhealth")
    @Config.RequiresWorldRestart
    public static boolean scaleMaxHealth = false;

    @Config.Comment("If true, max health will be capped at 6 hearts and absorption at 2 hearts per limb. If false, the health cap will be much higher (64 hearts normal and 16 absorption)")
    @Config.LangKey("firstaid.config.capmaxhealth")
    @Config.RequiresWorldRestart
    public static boolean capMaxHealth = true;

    public static class ExternalHealing {

        @Config.Comment("Allow vanilla's natural regeneration. Requires \"allowOtherHealingItems\" to be true" + "\n**WARNING** This sets the gamerule \"naturalRegeneration\" for all of your worlds internally, so it persists even if you remove the mod")
        @Config.LangKey("firstaid.config.allownaturalregeneration")
        @Config.RequiresWorldRestart
        public boolean allowNaturalRegeneration = false;

        @Config.Comment("If false, healing potions and other healing items will have no effect")
        @Config.LangKey("firstaid.config.allowotherhealingitems")
        public boolean allowOtherHealingItems = true;

        @Config.Comment("The total amount of health that will be distributed to all body parts after sleeping")
        @Config.LangKey("firstaid.config.sleephealing")
        @Config.RangeDouble(min = 0D, max = 20D)
        public float sleepHealing = 1F;

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

        @Config.Comment("True if the main health bar should be rendered (Will be average health)")
        @Config.LangKey("firstaid.config.showvanillahealthbar")
        public boolean showVanillaHealthBar = false;

        @Config.Comment("True if the overlay should be shown, false otherwise")
        @Config.LangKey("firstaid.config.showoverlay")
        public boolean showOverlay = true;

        @Config.Comment("If true the HUD will display the health as numbers instead of the \"normal\" icons")
        @Config.LangKey("firstaid.config.displayhealthasnumber")
        public boolean displayHealthAsNumber = false;

        @Config.Comment("A scaling option for the HUD in addition to minecraft's GUI scale")
        @Config.LangKey("firstaid.config.hudscale")
        @Config.RangeDouble(min = 0.2F, max = 2F)
        public float hudScale = 1F;

        @Config.Comment("The relative point of the overlay. 0=top+left, 1=top+right, 2=bottom+left, 3=bottom+right")
        @Config.LangKey("firstaid.config.position")
        @Config.RangeInt(min = 0, max = 3)
        public int position = 0;

        @Config.Comment("The offset on the x axis")
        @Config.LangKey("firstaid.config.xoffset")
        @ExtraConfig.Advanced
        public int xOffset = 0;

        @Config.Comment("The offset on the y axis")
        @Config.LangKey("firstaid.config.yoffset")
        @ExtraConfig.Advanced
        public int yOffset = 1;

        @Config.Comment("If the player has more hearts than the threshold, the health will be displayed as a number")
        @Config.LangKey("firstaid.config.heartthreshold")
        @ExtraConfig.Advanced
        public int heartThreshold = 8;
    }

    public static class DamageSystem {

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthHead = 4;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthLeftArm = 4;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthLeftLeg = 4;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthLeftFoot = 4;

        @Config.RangeInt(min = 2, max = 12)
        public int maxHealthBody = 6;

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
