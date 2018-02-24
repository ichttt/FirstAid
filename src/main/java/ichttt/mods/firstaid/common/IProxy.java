package ichttt.mods.firstaid.common;

import net.minecraft.util.EnumHand;

public interface IProxy {
    default void preInit() {}

    void init();

    default void showGuiApplyHealth(EnumHand activeHand) {}
}
