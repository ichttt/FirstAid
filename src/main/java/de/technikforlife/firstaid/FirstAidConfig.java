package de.technikforlife.firstaid;

import net.minecraftforge.common.config.Config;

@Config(modid = FirstAid.MODID, name = FirstAid.NAME)
public class FirstAidConfig {

    @Config.RequiresWorldRestart
    @Config.Comment("Disables natural Regeneration on world start")
    public static boolean allowNaturalRegeneration = false;
}
