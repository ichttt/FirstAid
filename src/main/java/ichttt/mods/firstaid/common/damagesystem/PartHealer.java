package ichttt.mods.firstaid.common.damagesystem;

import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import net.minecraft.item.ItemStack;

public class PartHealer extends AbstractPartHealer {
    private int ticksPassed = 0;
    private int heals = 0;

    public PartHealer(int ticksPerHeal, int maxHeal, ItemStack stack) {
        super(maxHeal, stack, ticksPerHeal);
    }

    @Override
    public AbstractPartHealer loadNBT(int ticksPassed, int heals) {
        this.ticksPassed = ticksPassed;
        this.heals = heals;
        return this;
    }

    @Override
    public boolean hasFinished() {
        return heals >= maxHeal;
    }

    @Override
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

    @Override
    public int getTicksPassed() {
        return ticksPassed;
    }

    @Override
    public int getHealsDone() {
        return heals;
    }
}
