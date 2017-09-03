package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;

public class PartHealer {
    private final EnumHealingType healingType;

    public final int maxHeal, ticksPerHeal;
    int ticksPassed = 0;
    int heals = 0;

    public PartHealer(int ticksPerHeal, int maxHeal, EnumHealingType type) {
        this.maxHeal = maxHeal;
        this.ticksPerHeal = ticksPerHeal;
        this.healingType = type;
    }

    public PartHealer loadNBT(int ticksPassed, int heals) {
        this.ticksPassed = ticksPassed;
        this.heals = heals;
        return this;
    }

    public boolean hasFinished() {
        return heals >= maxHeal;
    }

    public boolean tick() {
        if (hasFinished())
            return false;
        ticksPassed++;
        boolean nextHeal = ticksPassed >= ticksPerHeal;
        if (nextHeal) {
            ticksPassed = 0;
            heals++;
        }
        return nextHeal;
    }

    public EnumHealingType toEnum() {
        return healingType;
    }
}
