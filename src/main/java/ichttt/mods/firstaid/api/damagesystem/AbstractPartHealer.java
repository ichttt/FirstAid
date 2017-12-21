package ichttt.mods.firstaid.api.damagesystem;

import net.minecraft.item.ItemStack;

public abstract class AbstractPartHealer {
    public final int maxHeal;
    public final ItemStack stack;
    public final int ticksPerHeal;

    public AbstractPartHealer(int maxHeal, ItemStack stack, int ticksPerHeal) {
        this.maxHeal = maxHeal;
        this.stack = stack;
        this.ticksPerHeal = ticksPerHeal;
    }

    /**
     * Called when the part is loaded with saved data.
     *
     * @return this
     */
    public abstract AbstractPartHealer loadNBT(int ticksPassed, int heals);

    /**
     * Returns true if the healer is finished healing the body part.
     * The healer will be removed from the part at the next tick
     *
     * @return True if the healer is finished, otherwise false
     */
    public abstract boolean hasFinished();

    /**
     * Updates the healer.
     * Should not be called by other mods!
     */
    public abstract boolean tick();

    /**
     * Gets the time that passed since the
     */
    public abstract int getTicksPassed();

    /**
     * Gets the heals that this healer did
     */
    public abstract int getHealsDone();
}
