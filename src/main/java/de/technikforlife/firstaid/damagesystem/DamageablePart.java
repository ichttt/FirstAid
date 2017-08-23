package de.technikforlife.firstaid.damagesystem;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.UUID;

public class DamageablePart {

    public final float maxHealth;
    public final boolean canCauseDeath;
    public boolean hasBandage;
    private float healedWithBandage;

    @Nonnull
    private EnumWoundState state = EnumWoundState.HEALTHY;
    public float currentHealth;
    private final UUID playerUUID;

    public DamageablePart(float maxHealth, boolean canCauseDeath, UUID playerUUID) {
        this.maxHealth = maxHealth;
        this.canCauseDeath = canCauseDeath;
        currentHealth = maxHealth;
        this.playerUUID = playerUUID;
    }

    public EnumWoundState getWoundState() {
        return state;
    }

    /**
     * @return true if the {@link EnumWoundState} has changed
     */
    public boolean heal(float amount, EntityLivingBase toHeal) {
        EnumWoundState prev = state;
        currentHealth = Math.min(maxHealth, amount + currentHealth);
        state = EnumWoundState.getWoundState(maxHealth, currentHealth);
        toHeal.heal(amount);
        return prev != state;
    }

    /**
     * @return true if the player drops below/ has 0 HP
     */
    public boolean damage(float amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        return currentHealth == 0;
    }

    public void tick(World world) {
        EntityPlayer player = world.getPlayerEntityByUUID(playerUUID);
        if (player == null)
            return;
        if (hasBandage) {
            if (healedWithBandage >= maxHealth / 2) {
                hasBandage = false;
                healedWithBandage = 0F;
            } else {
                float healAmount = maxHealth / 800;
                heal(healAmount, player);
                healedWithBandage += healAmount;
            }
        }
    }

    public void bandage() {

    }
}
