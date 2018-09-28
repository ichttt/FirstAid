package ichttt.mods.firstaid.common;

import net.minecraft.util.EnumHand;

public interface IProxy {
    void preInit();

    default void init() {}

    default void showGuiApplyHealth(EnumHand activeHand) {}

    void throwWrongPlayerRevivalException();
}
