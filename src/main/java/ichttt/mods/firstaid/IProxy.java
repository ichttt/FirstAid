package ichttt.mods.firstaid;

import ichttt.mods.firstaid.api.enums.EnumHealingType;
import net.minecraft.util.EnumHand;

public interface IProxy {
    void init();

    default void showGuiApplyHealth(EnumHealingType healingType, EnumHand activeHand) {}
}
