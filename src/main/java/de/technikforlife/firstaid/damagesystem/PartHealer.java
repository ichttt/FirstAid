package de.technikforlife.firstaid.damagesystem;

public enum PartHealer {
    BANDAGE(400, 3), PLASTER(500, 2);

    public int maxHealth, ticksPerHeal;
    private int ticksPassed;
    private int heals = 0;

    PartHealer(int ticksPerHeal, int maxHealth) {
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
