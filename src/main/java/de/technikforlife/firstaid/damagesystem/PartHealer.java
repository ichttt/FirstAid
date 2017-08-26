package de.technikforlife.firstaid.damagesystem;

public class PartHealer {
    public static PartHealer getNewBandage() {
        return new PartHealer(400, 3);
    }

    public static PartHealer getNewPlaster() {
        return new PartHealer(500, 2);
    }

    public final int maxHealth, ticksPerHeal;
    private int ticksPassed;
    private int heals = 0;

    public PartHealer(int ticksPerHeal, int maxHealth) {
        this.maxHealth = maxHealth;
        this.ticksPerHeal = ticksPerHeal;
    }

    public boolean hasFinished() {
        return heals >= maxHealth;
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
}
