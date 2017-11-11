package ichttt.mods.firstaid.api.damagesystem;

import ichttt.mods.firstaid.api.enums.EnumHealingType;

public abstract class AbstractPartHealer {
    public final EnumHealingType healingType;
    public final int maxHeal;
    public final int ticksPerHeal;

    public AbstractPartHealer(int maxHeal, EnumHealingType type, int ticksPerHeal) {
        this.maxHeal = maxHeal;
        this.healingType = type;
        this.ticksPerHeal = ticksPerHeal;
    }

    public abstract AbstractPartHealer loadNBT(int ticksPassed, int heals);

    public abstract boolean hasFinished();

    public abstract boolean tick();

    public abstract int getTicksPassed();

    public abstract int getHealsDone();
}
