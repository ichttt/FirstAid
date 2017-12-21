package ichttt.mods.firstaid.common;

import net.minecraft.util.EnumHand;

public interface IProxy {
    void init();

    default void showGuiApplyHealth(EnumHand activeHand) {
    }
}
