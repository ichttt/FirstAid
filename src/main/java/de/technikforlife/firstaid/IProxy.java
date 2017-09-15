package de.technikforlife.firstaid;

import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;
import net.minecraft.util.EnumHand;

public interface IProxy {
    void init();

    default void showGuiApplyHealth(EnumHealingType healingType, EnumHand activeHand) {}

    default void healClient(float amount) {}
}
