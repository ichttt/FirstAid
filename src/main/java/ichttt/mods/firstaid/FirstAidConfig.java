package ichttt.mods.firstaid;

import net.minecraftforge.common.config.Config;

@SuppressWarnings("CanBeFinal")
@Config(modid = FirstAid.MODID, name = FirstAid.NAME)
public class FirstAidConfig {

    @Config.Comment("Settings regarding the max health of the body's parts")
    @Config.RequiresWorldRestart
    public static final DamageSystem damageSystem = new DamageSystem();

    @Config.Comment("Settings regarding the health overlay when ingame")
    public static final Overlay overlay = new Overlay();

    @Config.Comment("Settings regarding external healing system(like vanilla potions or natural regeneration")
    public static final ExternalHealing externalHealing = new ExternalHealing();

    @Config.Comment("False if the tutorial message should be shown. Client side only")
    public static boolean hasTutorial = false;

    @Config.Comment("Set to false to disable dynamic debuffs based on the health. Makes morphine useless")
    public static boolean enableDebuffs = true;

    @Config.Comment("Set to true to enable the debuff sounds. Requieres enableDebuffs to be true")
    public static boolean enableSoundSystem = true;

    public static class ExternalHealing {

        @Config.RequiresWorldRestart
        @Config.Comment("Allow vanilla's natural regeneration. Requires \"allowOtherHealingItems\" to be true")
        public boolean allowNaturalRegeneration = false;

        @Config.Comment("If false, healing potions and other healing items will have no effect")
        public boolean allowOtherHealingItems = true;

        @Config.Comment("The value external regen will be multiplied with. Has no effect if allowOtherHealingItems is disabled")
        @Config.RangeDouble(min = 0D)
        public double otherRegenMultiplier = 0.75D;

        @Config.Comment("The value vanilla's natural regeneration will be multiplied with. Has no effect if allowNaturalRegeneration is disabled")
        @Config.RangeDouble(min = 0D)
        public double naturalRegenMultiplier = 0.5D;
    }

    public static class Overlay {

        @Config.Comment("True if the main health bar should be rendered (Will be average health)")
        public boolean showVanillaHealthBar = false;

        @Config.Comment("True if the overlay should be shown, false otherwise")
        public boolean showOverlay = true;

        @Config.RangeInt(min = 0, max = 3)
        @Config.Comment("The relative point of the overlay. 0=top+left, 1=top+right, 2=bottom+left, 3=bottom+right")
        public int position = 0;

        @Config.Comment("The offset on the x axis")
        public int xOffset = 0;

        @Config.Comment("The offset on the y axis")
        public int yOffset = 1;
    }

    public static class DamageSystem {

        @Config.RangeInt(min = 0, max = 12)
        public int maxHealthHead = 4;

        @Config.RangeInt(min = 0, max = 12)
        public int maxHealthLeftArm = 4;

        @Config.RangeInt(min = 0, max = 12)
        public int maxHealthLeftLeg = 4;

        @Config.RangeInt(min = 0, max = 12)
        public int maxHealthLeftFoot = 4;

        @Config.RangeInt(min = 0, max = 12)
        public int maxHealthBody = 6;

        @Config.RangeInt(min = 0, max = 12)
        public int maxHealthRightArm = 4;

        @Config.RangeInt(min = 0, max = 12)
        public int maxHealthRightLeg = 4;

        @Config.RangeInt(min = 0, max = 12)
        public int maxHealthRightFoot = 4;
    }
}
