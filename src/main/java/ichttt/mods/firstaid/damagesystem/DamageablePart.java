package ichttt.mods.firstaid.damagesystem;

import ichttt.mods.firstaid.api.AbstractDamageablePart;
import ichttt.mods.firstaid.api.PartHealer;
import ichttt.mods.firstaid.damagesystem.debuff.ConstantDebuff;
import ichttt.mods.firstaid.damagesystem.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumHealingType;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class DamageablePart extends AbstractDamageablePart {
    private int maxHealth;
    @Nonnull
    private final IDebuff[] debuffs;
    private float absorption;

    public DamageablePart(int maxHealth, boolean canCauseDeath, @Nonnull EnumPlayerPart playerPart, @Nonnull IDebuff... debuffs) {
        super(maxHealth, canCauseDeath, playerPart);
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.debuffs = debuffs;
    }

    @Override
    public float heal(float amount, EntityPlayer player, boolean applyDebuff) {
        float notFitting = Math.abs(Math.min(0F, maxHealth - (currentHealth + amount)));
        currentHealth = Math.min(maxHealth, currentHealth + amount);
        if (notFitting > 0) {
            float oldHealth = currentHealth;
            currentHealth = Math.min(currentHealth + notFitting, currentHealth);
            notFitting = notFitting - (currentHealth - oldHealth);
        }
        final float finalNotFitting = notFitting;
        if (applyDebuff)
            Arrays.stream(debuffs).forEach(debuff -> debuff.handleHealing(amount - finalNotFitting, currentHealth / maxHealth, (EntityPlayerMP) player));
        return notFitting;
    }

    @Override
    public float damage(float amount, EntityPlayer player, boolean applyDebuff) {
        float origAmount = amount;
        if (absorption > 0) {
            amount = Math.abs(Math.min(0, absorption - origAmount));
            absorption = Math.max(0, absorption - origAmount);
        }
        float notFitting = Math.abs(Math.min(0, currentHealth - amount));
        currentHealth = Math.max(0, currentHealth - amount);
        if (applyDebuff)
            Arrays.stream(debuffs).forEach(debuff -> debuff.handleDamageTaken(origAmount - notFitting, currentHealth / maxHealth, (EntityPlayerMP) player));
        return notFitting;
    }

    @Override
    public void tick(World world, EntityPlayer player, boolean tickDebuffs) {
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

    @Override
    public void applyItem(PartHealer healer) {
        activeHealer = healer;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setFloat("health", currentHealth);
        if (absorption > 0F)
            compound.setFloat("absorption", absorption);
        if (activeHealer != null) {
            compound.setByte("healingItem", (byte) (activeHealer.healingType.ordinal() + 1)); //+1 because of backward compat
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
            activeHealer = EnumHealingType.VALUES[nbt.getByte("healingItem") - 1].createNewHealer().loadNBT(nbt.getInteger("itemTicks"), nbt.getInteger("itemHeals"));
        if (nbt.hasKey("absorption"))
            absorption = nbt.getFloat("absorption");
        //kick constant debuffs active
        Arrays.stream(debuffs).forEach(debuff -> debuff.handleHealing(0F, currentHealth / maxHealth, null));
    }

    @Override
    public void setAbsorption(float absorption) {
        this.absorption = absorption;
        currentHealth = Math.min(maxHealth + absorption, currentHealth);
    }

    @Override
    public float getAbsorption() {
        return absorption;
    }

    @Override
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(2, maxHealth); //set 2 as a minimum
        this.currentHealth = Math.min(currentHealth, this.maxHealth);
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }
}
