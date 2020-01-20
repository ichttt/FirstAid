package ichttt.mods.firstaid.api.damagesystem;

import ichttt.mods.firstaid.api.debuff.IDebuff;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public abstract class DamageablePart implements INBTSerializable<NBTTagCompound> {
    public final int initialMaxHealth;
    public final boolean canCauseDeath;

    public DamageablePart(int maxHealth, boolean canCauseDeath) {
        this.initialMaxHealth = maxHealth;
        this.canCauseDeath = canCauseDeath;
    }

    /**
     * Heals the part for the specified amount.
     *
     * @param amount      The amount the part should be healed, clamped to max health
     * @param entity      The entity that this part belongs to. May be null if applyDebuff is false, otherwise this is required nonnull
     * @param applyDebuff If all registered debuffs should be notified of the healing done
     * @return The amount of health that could not be added
     */
    public abstract float heal(float amount, @Nullable EntityLivingBase entity, boolean applyDebuff);

    /**
     * Damages the part for the specified amount.
     *
     * @param amount      The amount the part should be damaged, clamped to 0
     * @param entity      The entity that this part belongs to. May be null if applyDebuff is false, otherwise this is required nonnull
     * @param applyDebuff If all registered debuffs should be notified of the damage taken
     * @return The amount of damage that could not be done
     */
    public abstract float damage(float amount, @Nullable EntityLivingBase entity, boolean applyDebuff);

    /**
     * Damages the part for the specified amount.
     *
     * @param amount      The amount the part should be damaged, clamped to minHealth
     * @param entity      The entity that this part belongs to. May be null if applyDebuff is false, otherwise this is required nonnull
     * @param applyDebuff If all registered debuffs should be notified of the damage taken
     * @param minHealth   The minimum health the part should drop to
     * @return The amount of damage that could not be done
     */
    public abstract float damage(float amount, @Nullable EntityLivingBase entity, boolean applyDebuff, float minHealth);

    /**
     * Updates the part.
     * Should not be called by other mods!
     */
    public abstract void tick(World world, EntityLivingBase entity, boolean tickDebuffs);

    public abstract void setAbsorption(float absorption);

    public abstract float getAbsorption();

    public abstract void setMaxHealth(int maxHealth);

    public abstract int getMaxHealth();

    public abstract void setActiveHealer(@Nullable PartHealer activeHealer);

    @Nullable
    public abstract PartHealer getActiveHealer();

    public abstract void setCurrentHealth(float currentHealth);

    public abstract float getCurrentHealth();

    public abstract String getName();

    public abstract IDebuff[] getDebuffs();
}
