package de.technikforlife.firstaid;

import net.minecraftforge.common.config.Config;

@Config(modid = FirstAid.MODID, name = FirstAid.NAME)
public class FirstAidConfig {

    @Config.RequiresWorldRestart
    @Config.Comment("Disables natural Regeneration on world start")
    public static boolean allowNaturalRegeneration = false;

    @Config.RangeDouble(min = 0, max = Float.MAX_VALUE)
    @Config.Comment("How much bandage heals")
    public static double healAmount = 2D;
}
