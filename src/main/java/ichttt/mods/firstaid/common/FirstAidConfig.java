package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import net.minecraftforge.common.config.Config;

@SuppressWarnings("CanBeFinal")
@Config(modid = FirstAid.MODID, name = FirstAid.NAME)
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
    public static final Debuffs debuffs = new Debuffs();

    @Config.Comment("Set to true to enable the debuff sounds. Requieres enableDebuffs to be true")
    @Config.LangKey("firstaid.config.enablesoundsystem")
    public static boolean enableSoundSystem = true;

    @Config.Comment("If true, max health is scaled to your hearts, and the config entries get multiplier to match the max health")
    @Config.LangKey("firstaid.config.scalemaxhealth")
    @Config.RequiresWorldRestart
    public static boolean scaleMaxHealth = false;

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
        public double otherRegenMultiplier = 0.75D;

        @Config.Comment("The value vanilla's natural regeneration will be multiplied with. Has no effect if \"allowNaturalRegeneration\" is disabled")
        @Config.LangKey("firstaid.config.naturalregenmultiplier")
        @Config.RangeDouble(min = 0D, max = 20D)
        public double naturalRegenMultiplier = 0.5D;
    }

    public static class Overlay {

        @Config.Comment("True if the main health bar should be rendered (Will be average health)")
        @Config.LangKey("firstaid.config.showvanillahealthbar")
        public boolean showVanillaHealthBar = false;

        @Config.Comment("True if the overlay should be shown, false otherwise")
        @Config.LangKey("firstaid.config.showoverlay")
        public boolean showOverlay = true;

        public boolean displayHealthAsNumber = false;

        @Config.RangeDouble(min = 0.2F, max = 2F)
        public float hudScale = 1F;

        @Config.Comment("The relative point of the overlay. 0=top+left, 1=top+right, 2=bottom+left, 3=bottom+right")
        @Config.LangKey("firstaid.config.position")
        @Config.RangeInt(min = 0, max = 3)
        public int position = 0;

        @Config.Comment("The offset on the x axis")
        @Config.LangKey("firstaid.config.xoffset")
        public int xOffset = 0;

        @Config.Comment("The offset on the y axis")
        @Config.LangKey("firstaid.config.yoffset")
        public int yOffset = 1;
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

            public boolean nausea = true;
        }

        public static class Body {
            public boolean nausea = true;

            public boolean weakness = true;
        }

        public static class Arms {
            public boolean mining_fatigue = true;
        }

        public static class LegsAndFeet {
            public boolean slowness = true;
        }
    }
}
