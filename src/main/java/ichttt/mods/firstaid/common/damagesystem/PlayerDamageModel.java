/*
 * FirstAid
 * Copyright (C) 2017-2018
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.common.damagesystem;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.DataManagerWrapper;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class PlayerDamageModel extends AbstractPlayerDamageModel {
    private final Set<SharedDebuff> sharedDebuffs = new HashSet<>();
    private int morphineTicksLeft = 0;
    private int sleepBlockTicks = 0;
    private float prevHealthCurrent = -1F;
    private float prevScaleFactor;
    private boolean waitingForHelp = false;
    private final boolean noCritical;
    private boolean needsMorphineUpdate = false;

    public static PlayerDamageModel create() {
        FirstAidRegistry registry = FirstAidRegistryImpl.INSTANCE;
        IDebuff[] headDebuffs = registry.getDebuffs(EnumDebuffSlot.HEAD);
        IDebuff[] bodyDebuffs = registry.getDebuffs(EnumDebuffSlot.BODY);
        IDebuff[] armsDebuffs = registry.getDebuffs(EnumDebuffSlot.ARMS);
        IDebuff[] legFootDebuffs = registry.getDebuffs(EnumDebuffSlot.LEGS_AND_FEET);
        return new PlayerDamageModel(headDebuffs, bodyDebuffs, armsDebuffs, legFootDebuffs);
    }

    protected PlayerDamageModel(IDebuff[] headDebuffs, IDebuff[] bodyDebuffs, IDebuff[] armDebuffs, IDebuff[] legFootDebuffs) {
        super(new DamageablePart(FirstAidConfig.damageSystem.maxHealthHead,      FirstAidConfig.damageSystem.causeDeathHead,  EnumPlayerPart.HEAD,       headDebuffs   ),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftArm,   false,                         EnumPlayerPart.LEFT_ARM,   armDebuffs    ),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftLeg,   false,                         EnumPlayerPart.LEFT_LEG,   legFootDebuffs),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftFoot,  false,                         EnumPlayerPart.LEFT_FOOT,  legFootDebuffs),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthBody,      FirstAidConfig.damageSystem.causeDeathBody,  EnumPlayerPart.BODY,       bodyDebuffs   ),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightArm,  false,                         EnumPlayerPart.RIGHT_ARM,  armDebuffs    ),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightLeg,  false,                         EnumPlayerPart.RIGHT_LEG,  legFootDebuffs),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightFoot, false,                         EnumPlayerPart.RIGHT_FOOT, legFootDebuffs));
        for (IDebuff debuff : armDebuffs)
            this.sharedDebuffs.add((SharedDebuff) debuff);
        for (IDebuff debuff : legFootDebuffs)
            this.sharedDebuffs.add((SharedDebuff) debuff);
        noCritical = !FirstAidConfig.damageSystem.causeDeathBody && !FirstAidConfig.damageSystem.causeDeathHead;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.put("head", HEAD.serializeNBT());
        tagCompound.put("leftArm", LEFT_ARM.serializeNBT());
        tagCompound.put("leftLeg", LEFT_LEG.serializeNBT());
        tagCompound.put("leftFoot", LEFT_FOOT.serializeNBT());
        tagCompound.put("body", BODY.serializeNBT());
        tagCompound.put("rightArm", RIGHT_ARM.serializeNBT());
        tagCompound.put("rightLeg", RIGHT_LEG.serializeNBT());
        tagCompound.put("rightFoot", RIGHT_FOOT.serializeNBT());
        tagCompound.putBoolean("hasTutorial", hasTutorial);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        HEAD.deserializeNBT((NBTTagCompound) nbt.get("head"));
        LEFT_ARM.deserializeNBT((NBTTagCompound) nbt.get("leftArm"));
        LEFT_LEG.deserializeNBT((NBTTagCompound) nbt.get("leftLeg"));
        LEFT_FOOT.deserializeNBT((NBTTagCompound) nbt.get("leftFoot"));
        BODY.deserializeNBT((NBTTagCompound) nbt.get("body"));
        RIGHT_ARM.deserializeNBT((NBTTagCompound) nbt.get("rightArm"));
        RIGHT_LEG.deserializeNBT((NBTTagCompound) nbt.get("rightLeg"));
        RIGHT_FOOT.deserializeNBT((NBTTagCompound) nbt.get("rightFoot"));
        if (nbt.contains("morphineTicks")) { //legacy - we still have to write it
            morphineTicksLeft = nbt.getInt("morphineTicks");
            needsMorphineUpdate = true;
        }
        if (nbt.contains("hasTutorial"))
            hasTutorial = nbt.getBoolean("hasTutorial");
    }

    @Override
    public void tick(World world, EntityPlayer player) {
        if (isDead(player))
            return;
        world.profiler.startSection("FirstAidPlayerModel");
        if (sleepBlockTicks > 0)
            sleepBlockTicks--;
        else if (sleepBlockTicks < 0)
            throw new RuntimeException("Negative sleepBlockTicks " + sleepBlockTicks);

        float currentHealth = getCurrentHealth();
        if (currentHealth <= 0F) {
            FirstAid.LOGGER.error("Got {} health left, but isn't marked as dead!", currentHealth);
            world.profiler.endSection();
            return;
        }

        float newCurrentHealth = (currentHealth / getCurrentMaxHealth()) * player.getMaxHealth();

        if (Float.isInfinite(newCurrentHealth)) {
            FirstAid.LOGGER.error("Error calculating current health: Value was infinite"); //Shouldn't happen anymore, but let's be safe
        } else {
            if (newCurrentHealth != prevHealthCurrent)
                ((DataManagerWrapper) player.dataManager).set_impl(EntityPlayer.HEALTH, newCurrentHealth);
            prevHealthCurrent = newCurrentHealth;
        }

        if (!this.hasTutorial)
            this.hasTutorial = CapProvider.tutorialDone.contains(player.getName().getString());

        if (FirstAidConfig.scaleMaxHealth) { //Attempt to calculate the max health of the body parts based on the maxHealth attribute
            world.profiler.startSection("healthscaling");
            float globalFactor = player.getMaxHealth() / 20F;
            if (prevScaleFactor != globalFactor) {
                int reduced = 0;
                int added = 0;
                for (AbstractDamageablePart part : this) {
                    int result = Math.round(part.initialMaxHealth * globalFactor);
                    if (result % 2 == 1) {
                        int partMaxHealth = part.getMaxHealth();
                        if (part.currentHealth < partMaxHealth && reduced < 4) {
                            result--;
                            reduced++;
                        } else if (part.currentHealth > partMaxHealth && added < 4) {
                            result++;
                            added++;
                        } else if (reduced > added) {
                            result++;
                            added++;
                        } else {
                            result--;
                            reduced++;
                        }
                    }
                    part.setMaxHealth(result);
                }
            }
            prevScaleFactor = globalFactor;
            world.profiler.endSection();
        }

        //morphine update
        if (this.needsMorphineUpdate) {
            player.addPotionEffect(new PotionEffect(EventHandler.MORPHINE, this.morphineTicksLeft, 0, false, false));
        }
        PotionEffect morphine = player.getActivePotionEffect(EventHandler.MORPHINE);
        if (!this.needsMorphineUpdate) {
            this.morphineTicksLeft = morphine == null ? 0 : morphine.getDuration();
        }
        this.needsMorphineUpdate = false;

        //Debuff and part ticking
        world.profiler.startSection("PartDebuffs");
        forEach(part -> part.tick(world, player, morphine == null));
        if (morphine == null)
            sharedDebuffs.forEach(sharedDebuff -> sharedDebuff.tick(player));
        world.profiler.endSection();
        world.profiler.endSection();
    }

    public static int getRandMorphineDuration() { //Tweak tooltip event when changing as well
        return ((EventHandler.rand.nextInt(5) * 20 * 15) + 20 * 210);
    }

    @Deprecated
    @Override
    public void applyMorphine() {
        morphineTicksLeft = getRandMorphineDuration();
        needsMorphineUpdate = true;
    }

    @Override
    public void applyMorphine(EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(EventHandler.MORPHINE, getRandMorphineDuration(), 0, false, false));
    }

    @Deprecated
    @Override
    public int getMorphineTicks() {
        return morphineTicksLeft;
    }

    @Override
    @Nonnull
    public Iterator<AbstractDamageablePart> iterator() {
        return new Iterator<AbstractDamageablePart>() {
            byte count = 1;
            @Override
            public boolean hasNext() {
                return count <= 8;
            }

            @Override
            public AbstractDamageablePart next() {
                if (count > 8)
                    throw new NoSuchElementException();
                AbstractDamageablePart part = getFromEnum(EnumPlayerPart.fromID(count));
                count++;
                return part;
            }
        };
    }

    @Override
    public float getCurrentHealth() {
        float currentHealth = 0;
        for (AbstractDamageablePart part : this)
            currentHealth += part.currentHealth;
        return currentHealth;
    }

    @Override
    public boolean isDead(@Nullable EntityPlayer player) {
//        IRevival revival = CommonUtils.getRevivalIfPossible(player); TODO playerrevival compat
//        if (revival != null) {
//            if (!revival.isHealty() && !revival.isDead()) {
//                this.waitingForHelp = true; //Technically not dead yet, but we should still return true
//                return true;
//            } else if (this.waitingForHelp && revival.isRevived()) { //We just got revived
//                this.waitingForHelp = false;
//                player.isDead = false;
//                for (AbstractDamageablePart part : this) {
//                    if ((part.canCauseDeath || this.noCritical) && part.currentHealth <= 0F) {
//                        part.currentHealth = 1F; // Set the critical health to a non-zero value
//                    }
//                }
///                //make sure to resync the client health
//                if (!player.world.isRemote && player instanceof EntityPlayerMP)
//                    FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(this), (EntityPlayerMP) player); //Upload changes to the client
//                return false;
//            }
//        }

        if (player != null && (player.removed || player.getHealth() <= 0F))
            return true;

        if (this.noCritical) {
            boolean dead = true;
            for (AbstractDamageablePart part : this) {
                if (part.currentHealth > 0) {
                    dead = false;
                    break;
                }
            }
            return dead;
        } else {
            for (AbstractDamageablePart part : this) {
                if (part.canCauseDeath && part.currentHealth <= 0) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Float getAbsorption() { //Float class because of DataManager
        float value = 0;
        for (AbstractDamageablePart part : this)
                value += part.getAbsorption();
        return value; //Autoboxing FTW
    }

    @Override
    public void setAbsorption(float absorption) {
        final float newAbsorption = absorption / 8F;
        forEach(damageablePart -> damageablePart.setAbsorption(newAbsorption));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getMaxRenderSize() {
        int max = 0;
        for (AbstractDamageablePart part : this) {
            int newMax;
            if (FirstAidConfig.overlay.overlayMode == FirstAidConfig.Overlay.OverlayMode.NUMBERS)
                newMax = Minecraft.getInstance().fontRenderer.getStringWidth(HealthRenderUtils.TEXT_FORMAT.format(part.currentHealth) + "/" + part.getMaxHealth()) + 1;
            else
                newMax = (int) (((((int) (part.getMaxHealth() + part.getAbsorption() + 0.9999F)) + 1) / 2F) * 9F);
            max = Math.max(max, newMax);
        }
        return max;
    }

    @Override
    public void sleepHeal(EntityPlayer player) {
        if (sleepBlockTicks > 0)
            return;
        CommonUtils.healPlayerByPercentage(FirstAidConfig.externalHealing.sleepHealPercentage, this, player);
        sleepBlockTicks = 20;
    }

    @Override
    public int getCurrentMaxHealth() {
        int maxHealth = 0;
        for (AbstractDamageablePart part : this) {
            maxHealth += part.getMaxHealth();
        }
        return maxHealth;
    }
}
