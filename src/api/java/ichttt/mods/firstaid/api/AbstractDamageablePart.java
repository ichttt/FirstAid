package ichttt.mods.firstaid.api;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractDamageablePart implements INBTSerializable<NBTTagCompound> {
    public final int initialMaxHealth;
    public final boolean canCauseDeath;
    @Nonnull
    public final EnumPlayerPart part;
    @Nullable
    public PartHealer activeHealer;
    public float currentHealth;

    public AbstractDamageablePart(int maxHealth, boolean canCauseDeath, @Nonnull EnumPlayerPart playerPart) {
        this.initialMaxHealth = maxHealth;
        this.canCauseDeath = canCauseDeath;
        this.part = playerPart;
    }

    public abstract float heal(float amount, EntityPlayer player, boolean applyDebuff);

    public abstract float damage(float amount, EntityPlayer player, boolean applyDebuff);

    public abstract void tick(World world, EntityPlayer player, boolean tickDebuffs);

    public abstract void applyItem(PartHealer healer);

    public abstract void setAbsorption(float absorption);

    public abstract float getAbsorption();

    public abstract void setMaxHealth(int maxHealth);

    public abstract int getMaxHealth();
}
