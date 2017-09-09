package de.technikforlife.firstaid;

import net.minecraftforge.common.config.Config;

@SuppressWarnings("CanBeFinal")
@Config(modid = FirstAid.MODID, name = FirstAid.NAME)
public class FirstAidConfig {


    @Config.Comment("Settings regarding the max health of the body's parts")
    @Config.RequiresWorldRestart
    public static final DamageSystem damageSystem = new DamageSystem();

    @Config.Comment("True if the tutorial message should be shown. Client side only")
    public static boolean hasTutorial = false;

    @SuppressWarnings("CanBeFinal")
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
