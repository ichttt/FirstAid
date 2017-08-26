package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.damagesystem.enums.EnumWoundState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageablePart {

    public final float maxHealth;
    public final boolean canCauseDeath;
    @Nullable
    private PartHealer activeHealer;

    @Nonnull
    private EnumWoundState state = EnumWoundState.HEALTHY;
    public float currentHealth;

    public DamageablePart(float maxHealth, boolean canCauseDeath) {
        this.maxHealth = maxHealth;
        this.canCauseDeath = canCauseDeath;
        currentHealth = maxHealth;
    }

    public EnumWoundState getWoundState() {
        return state;
    }

    public void heal(float amount, EntityLivingBase toHeal) {
        currentHealth = Math.min(maxHealth, amount + currentHealth);
        state = EnumWoundState.getWoundState(maxHealth, currentHealth);
        toHeal.heal(amount);
    }

    /**
     * @return true if the player drops below/ has 0 HP
     */
    public boolean damage(float amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        state = EnumWoundState.getWoundState(maxHealth, currentHealth);
        return currentHealth == 0;
    }

    void tick(World world, EntityPlayer player) {
        if (activeHealer != null) {
            if (activeHealer.tick()) {
                heal(1F, player);
                world.playEvent(2005, player.getPosition(), 0);
            }
            if (activeHealer.hasFinished())
                activeHealer = null;
        }
    }

    public void applyItem(PartHealer healer) {
        activeHealer = healer;
    }
}
