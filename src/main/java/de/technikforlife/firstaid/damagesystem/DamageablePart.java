package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;
import de.technikforlife.firstaid.damagesystem.enums.EnumWoundState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageablePart implements INBTSerializable<NBTTagCompound> {

    public final float maxHealth;
    public final boolean canCauseDeath;
    @Nullable
    public PartHealer activeHealer;
    @Nonnull
    public final EntityEquipmentSlot equipmentSlot;

    @Nonnull
    private EnumWoundState state = EnumWoundState.HEALTHY;
    public float currentHealth;

    public DamageablePart(float maxHealth, boolean canCauseDeath, @Nonnull EntityEquipmentSlot equipmentSlot) {
        this.maxHealth = maxHealth;
        this.canCauseDeath = canCauseDeath;
        currentHealth = maxHealth;
        this.equipmentSlot = equipmentSlot;
    }

    public EnumWoundState getWoundState() {
        return state;
    }

    public void heal(float amount) {
        currentHealth = Math.min(maxHealth, amount + currentHealth);
        state = EnumWoundState.getWoundState(maxHealth, currentHealth);
    }

    /**
     * @return true if the player drops below/ has 0 HP
     */
    public boolean damage(float amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        state = EnumWoundState.getWoundState(maxHealth, currentHealth);
        return currentHealth == 0;
    }

    void tick(World world, EntityPlayer player, boolean fake) {
        if (activeHealer != null) {
            if (activeHealer.tick()) {
                heal(1F);
                if (!fake) {
                    world.playEvent(2005, player.getPosition(), 0);
                }
            }
            if (activeHealer.hasFinished())
                activeHealer = null;
        }
    }

    public void applyItem(PartHealer healer) {
        activeHealer = healer;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setFloat("health", currentHealth);
        if (activeHealer != null) {
            compound.setByte("healingItem", (byte) activeHealer.healingType.id);
            compound.setInteger("itemTicks", activeHealer.ticksPassed);
            compound.setInteger("itemHeals", activeHealer.heals);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound nbt) {
        if (nbt == null)
            return;
        currentHealth = nbt.getFloat("health");
        if (nbt.hasKey("healingItem"))
            activeHealer = EnumHealingType.fromID(nbt.getByte("healingItem")).createNewHealer().loadNBT(nbt.getInteger("itemTicks"), nbt.getInteger("itemHeals"));
        state = EnumWoundState.getWoundState(maxHealth, currentHealth);
    }
}
