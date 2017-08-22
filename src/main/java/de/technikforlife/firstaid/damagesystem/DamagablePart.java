package de.technikforlife.firstaid.damagesystem;

import javax.annotation.Nonnull;

public class DamagablePart {

    public final float maxHealth;
    public final boolean canCauseDeath;

    @Nonnull
    private EnumWoundState state = EnumWoundState.HEALTHY;
    public float currentHealth;

    public DamagablePart(float maxHealth, boolean canCauseDeath) {
        this.maxHealth = maxHealth;
        this.canCauseDeath = canCauseDeath;
        currentHealth = maxHealth;
    }

    public EnumWoundState getWoundState() {
        return state;
    }

    /**
     * @return true if the {@link EnumWoundState} has changed
     */
    public boolean heal(float amount) {
        EnumWoundState prev = state;
        currentHealth = Math.min(maxHealth, amount + currentHealth);
        state = EnumWoundState.getWoundState(maxHealth, currentHealth);
        return prev != state;
    }

    /**
     * @return true if the player drops below/ has 0 HP
     */
    public boolean damage(float amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        return currentHealth == 0;
    }
}
