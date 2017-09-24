package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.damagesystem.debuff.ConstantDebuff;
import de.technikforlife.firstaid.damagesystem.debuff.IDebuff;
import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;
import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class DamageablePart implements INBTSerializable<NBTTagCompound> {

    public final float maxHealth;
    public final boolean canCauseDeath;
    @Nullable
    public PartHealer activeHealer;
    @Nonnull
    public final EnumPlayerPart part;
    @Nonnull
    private final IDebuff[] debuffs;

    public float currentHealth;

    public DamageablePart(float maxHealth, boolean canCauseDeath, @Nonnull EnumPlayerPart playerPart, @Nonnull IDebuff... debuffs) {
        this.maxHealth = maxHealth;
        this.canCauseDeath = canCauseDeath;
        this.currentHealth = maxHealth;
        this.part = playerPart;
        this.debuffs = debuffs;
    }

    public float heal(float amount, EntityPlayer player, boolean applyDebuff) {
        float notFitting = Math.abs(Math.min(0F, maxHealth - (currentHealth + amount)));
        currentHealth = Math.min(maxHealth, currentHealth + amount);
        if (applyDebuff)
            Arrays.stream(debuffs).forEach(debuff -> debuff.handleHealing(amount - notFitting, currentHealth / maxHealth, player));
        return notFitting;
    }

    public float damage(float amount, EntityPlayer player, boolean applyDebuff) {
        float notFitting = Math.abs(Math.min(0, currentHealth - amount));
        currentHealth = Math.max(0, currentHealth - amount);
        if (applyDebuff)
            Arrays.stream(debuffs).forEach(debuff -> debuff.handleDamageTaken(amount - notFitting, currentHealth / maxHealth, player));
        return notFitting;
    }

    void tick(World world, EntityPlayer player, boolean tickDebuffs) {
        if (activeHealer != null) {
            if (activeHealer.tick()) {
                heal(1F, player, !world.isRemote);
                if (!world.isRemote) {
                    world.playEvent(2005, player.getPosition(), 0);
                }
            }
            if (activeHealer.hasFinished())
                activeHealer = null;
        }
        if (!world.isRemote && tickDebuffs)
            Arrays.stream(debuffs).filter(debuff -> debuff instanceof ConstantDebuff).forEach(debuff -> ((ConstantDebuff) debuff).update(player));
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
        currentHealth = Math.min(maxHealth, nbt.getFloat("health"));
        if (nbt.hasKey("healingItem"))
            activeHealer = EnumHealingType.fromID(nbt.getByte("healingItem")).createNewHealer().loadNBT(nbt.getInteger("itemTicks"), nbt.getInteger("itemHeals"));
        //kick constant debuffs active
        Arrays.stream(debuffs).forEach(debuff -> debuff.handleHealing(0F, currentHealth / maxHealth, null));
    }
}
