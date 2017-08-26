package de.technikforlife.firstaid;

import net.minecraftforge.common.config.Config;

@Config(modid = FirstAid.MODID, name = FirstAid.NAME)
public class FirstAidConfig {

    @Config.RequiresWorldRestart
    @Config.Comment("Disable natural Regeneration on world start")
    public static boolean allowNaturalRegeneration = false;

    @Config.Comment("Settings regarding the max health of the body's parts")
    public static final DamageSystem damageSystem = new DamageSystem();

    public static class DamageSystem {

        @Config.RangeInt(min = 0, max = 16)
        public int maxHealthHead = 4;

        @Config.RangeInt(min = 0, max = 16)
        public int maxHealthLeftArm = 4;

        @Config.RangeInt(min = 0, max = 16)
        public int maxHealthLeftLeg = 4;

        @Config.RangeInt(min = 0, max = 16)
        public int maxHealthBody = 6;

        @Config.RangeInt(min = 0, max = 16)
        public int maxHealthRightArm = 4;

        @Config.RangeInt(min = 0, max = 16)
        public int maxHealthRightLeg = 4;
    }
}
