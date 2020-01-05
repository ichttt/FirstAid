/*
 * FirstAid
 * Copyright (C) 2017-2019
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
import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import ichttt.mods.firstaid.api.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumBodyPart;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.common.DataManagerWrapper;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class EntityDamageModelImpl implements EntityDamageModel {
    protected final DamageablePart head;
    protected final DamageablePart leftArm;
    protected final DamageablePart leftLeg;
    protected final DamageablePart leftFoot;
    protected final DamageablePart body;
    protected final DamageablePart rightArm;
    protected final DamageablePart rightLeg;
    protected final DamageablePart rightFoot;
    protected final Set<SharedDebuff> sharedDebuffs = new HashSet<>();
    protected final boolean noCritical;
    protected int morphineTicksLeft = 0;
    protected float prevHealthCurrent = -1F;
    protected float prevScaleFactor;
    protected boolean needsMorphineUpdate = false;

    public static <T extends EntityLivingBase> EntityDamageModel create() {
        FirstAidRegistry registry = FirstAidRegistryImpl.INSTANCE;
        IDebuff[] headDebuffs = registry.getDebuffs(EnumDebuffSlot.HEAD);
        IDebuff[] bodyDebuffs = registry.getDebuffs(EnumDebuffSlot.BODY);
        IDebuff[] armsDebuffs = registry.getDebuffs(EnumDebuffSlot.ARMS);
        IDebuff[] legFootDebuffs = registry.getDebuffs(EnumDebuffSlot.LEGS_AND_FEET);
        return new EntityDamageModelImpl(headDebuffs, bodyDebuffs, armsDebuffs, legFootDebuffs);
    }

    public static PlayerDamageModel createPlayer() {
        FirstAidRegistry registry = FirstAidRegistryImpl.INSTANCE;
        IDebuff[] headDebuffs = registry.getDebuffs(EnumDebuffSlot.HEAD);
        IDebuff[] bodyDebuffs = registry.getDebuffs(EnumDebuffSlot.BODY);
        IDebuff[] armsDebuffs = registry.getDebuffs(EnumDebuffSlot.ARMS);
        IDebuff[] legFootDebuffs = registry.getDebuffs(EnumDebuffSlot.LEGS_AND_FEET);
        return new PlayerDamageModelImpl(headDebuffs, bodyDebuffs, armsDebuffs, legFootDebuffs);
    }

    protected EntityDamageModelImpl(IDebuff[] headDebuffs, IDebuff[] bodyDebuffs, IDebuff[] armDebuffs, IDebuff[] legFootDebuffs) {
        this.head = new DamageablePartImpl(FirstAidConfig.damageSystem.maxHealthHead, FirstAidConfig.damageSystem.causeDeathHead, EnumBodyPart.HEAD, headDebuffs);
        this.leftArm = new DamageablePartImpl(FirstAidConfig.damageSystem.maxHealthLeftArm, false, EnumBodyPart.LEFT_ARM, armDebuffs);
        this.leftLeg = new DamageablePartImpl(FirstAidConfig.damageSystem.maxHealthLeftLeg, false, EnumBodyPart.LEFT_LEG, legFootDebuffs);
        this.leftFoot = new DamageablePartImpl(FirstAidConfig.damageSystem.maxHealthLeftFoot, false, EnumBodyPart.LEFT_FOOT, legFootDebuffs);
        this.body = new DamageablePartImpl(FirstAidConfig.damageSystem.maxHealthBody, FirstAidConfig.damageSystem.causeDeathBody, EnumBodyPart.BODY, bodyDebuffs);
        this.rightArm = new DamageablePartImpl(FirstAidConfig.damageSystem.maxHealthRightArm, false, EnumBodyPart.RIGHT_ARM, armDebuffs);
        this.rightLeg = new DamageablePartImpl(FirstAidConfig.damageSystem.maxHealthRightLeg, false, EnumBodyPart.RIGHT_LEG, legFootDebuffs);
        this.rightFoot = new DamageablePartImpl(FirstAidConfig.damageSystem.maxHealthRightFoot, false, EnumBodyPart.RIGHT_FOOT, legFootDebuffs);
        for (IDebuff debuff : armDebuffs)
            this.sharedDebuffs.add((SharedDebuff) debuff);
        for (IDebuff debuff : legFootDebuffs)
            this.sharedDebuffs.add((SharedDebuff) debuff);
        noCritical = !FirstAidConfig.damageSystem.causeDeathBody && !FirstAidConfig.damageSystem.causeDeathHead;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setTag("head", head.serializeNBT());
        tagCompound.setTag("leftArm", leftArm.serializeNBT());
        tagCompound.setTag("leftLeg", leftLeg.serializeNBT());
        tagCompound.setTag("leftFoot", leftFoot.serializeNBT());
        tagCompound.setTag("body", body.serializeNBT());
        tagCompound.setTag("rightArm", rightArm.serializeNBT());
        tagCompound.setTag("rightLeg", rightLeg.serializeNBT());
        tagCompound.setTag("rightFoot", rightFoot.serializeNBT());
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        head.deserializeNBT((NBTTagCompound) nbt.getTag("head"));
        leftArm.deserializeNBT((NBTTagCompound) nbt.getTag("leftArm"));
        leftLeg.deserializeNBT((NBTTagCompound) nbt.getTag("leftLeg"));
        leftFoot.deserializeNBT((NBTTagCompound) nbt.getTag("leftFoot"));
        body.deserializeNBT((NBTTagCompound) nbt.getTag("body"));
        rightArm.deserializeNBT((NBTTagCompound) nbt.getTag("rightArm"));
        rightLeg.deserializeNBT((NBTTagCompound) nbt.getTag("rightLeg"));
        rightFoot.deserializeNBT((NBTTagCompound) nbt.getTag("rightFoot"));
        if (nbt.hasKey("morphineTicks")) { //legacy - we still have to write it
            morphineTicksLeft = nbt.getInteger("morphineTicks");
            needsMorphineUpdate = true;
        }
    }

    @Override
    public DamageablePart getFromEnum(EnumBodyPart part) {
        switch (part) {
            case HEAD:
                return head;
            case LEFT_ARM:
                return leftArm;
            case LEFT_LEG:
                return leftLeg;
            case BODY:
                return body;
            case RIGHT_ARM:
                return rightArm;
            case RIGHT_LEG:
                return rightLeg;
            case LEFT_FOOT:
                return leftFoot;
            case RIGHT_FOOT:
                return rightFoot;
            default:
                throw new RuntimeException("Unknown enum " + part);
        }
    }

    @Override
    public boolean tick(World world, EntityLivingBase entity) {
        if (isDead(entity))
            return false;
        world.profiler.startSection("FirstAidPlayerModel");

        float currentHealth = getCurrentHealth();
        if (currentHealth <= 0F) {
            FirstAid.LOGGER.error("Got {} health left, but isn't marked as dead!", currentHealth);
            world.profiler.endSection();
            return false;
        }

        float newCurrentHealth = (currentHealth / getCurrentMaxHealth()) * entity.getMaxHealth();

        if (Float.isInfinite(newCurrentHealth)) {
            FirstAid.LOGGER.error("Error calculating current health: Value was infinite"); //Shouldn't happen anymore, but let's be safe
        } else {
            if (newCurrentHealth != prevHealthCurrent)
                ((DataManagerWrapper) entity.dataManager).set_impl(EntityPlayer.HEALTH, newCurrentHealth);
            prevHealthCurrent = newCurrentHealth;
        }

        runScaleLogic(entity);

        //morphine update
        if (this.needsMorphineUpdate) {
            entity.addPotionEffect(new PotionEffect(EventHandler.MORPHINE, this.morphineTicksLeft, 0, false, false));
        }
        PotionEffect morphine = entity.getActivePotionEffect(EventHandler.MORPHINE);
        if (!this.needsMorphineUpdate) {
            this.morphineTicksLeft = morphine == null ? 0 : morphine.getDuration();
        }
        this.needsMorphineUpdate = false;

        //Debuff and part ticking
        world.profiler.startSection("PartDebuffs");
        forEach(part -> part.tick(world, entity, morphine == null));
        if (morphine == null)
            sharedDebuffs.forEach(sharedDebuff -> sharedDebuff.tick(entity));
        world.profiler.endSection();
        world.profiler.endSection();
        return true;
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
    public void applyMorphine(EntityLivingBase entity) {
        entity.addPotionEffect(new PotionEffect(EventHandler.MORPHINE, getRandMorphineDuration(), 0, false, false));
    }

    @Deprecated
    @Override
    public int getMorphineTicks() {
        return morphineTicksLeft;
    }

    @Override
    @Nonnull
    public Iterator<DamageablePart> iterator() {
        return new Iterator<DamageablePart>() {
            byte count = 1;
            @Override
            public boolean hasNext() {
                return count <= 8;
            }

            @Override
            public DamageablePart next() {
                if (count > 8)
                    throw new NoSuchElementException();
                DamageablePart part = getFromEnum(EnumBodyPart.fromID(count));
                count++;
                return part;
            }
        };
    }

    @Override
    public float getCurrentHealth() {
        float currentHealth = 0;
        for (DamageablePart part : this)
            currentHealth += part.getCurrentHealth();
        return currentHealth;
    }

    @Override
    public boolean isDead(@Nullable EntityLivingBase entity) {
        if (entity != null && (entity.isDead || entity.getHealth() <= 0F))
            return true;

        if (this.noCritical) {
            boolean dead = true;
            for (DamageablePart part : this) {
                if (part.getCurrentHealth() > 0) {
                    dead = false;
                    break;
                }
            }
            return dead;
        } else {
            for (DamageablePart part : this) {
                if (part.canCauseDeath && part.getCurrentHealth() <= 0) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Float getAbsorption() { //Float class because of DataManager
        float value = 0;
        for (DamageablePart part : this)
                value += part.getAbsorption();
        return value; //Autoboxing FTW
    }

    @Override
    public void setAbsorption(float absorption) {
        final float newAbsorption = absorption / 8F;
        forEach(damageablePart -> damageablePart.setAbsorption(newAbsorption));
    }

    @Override
    public int getCurrentMaxHealth() {
        int maxHealth = 0;
        for (DamageablePart part : this) {
            maxHealth += part.getMaxHealth();
        }
        return maxHealth;
    }

    @Override
    public void runScaleLogic(EntityLivingBase entity) {
        if (FirstAidConfig.scaleMaxHealth) { //Attempt to calculate the max health of the body parts based on the maxHealth attribute
            entity.world.profiler.startSection("healthscaling");
            float globalFactor = entity.getMaxHealth() / 20F;
            if (prevScaleFactor != globalFactor) {
                if (FirstAidConfig.debug) {
                    FirstAid.LOGGER.info("Starting health scaling factor {} -> {} (max health {})", prevScaleFactor, globalFactor, entity.getMaxHealth());
                }
                entity.world.profiler.startSection("distribution");
                int reduced = 0;
                int added = 0;
                float expectedNewMaxHealth = 0F;
                int newMaxHealth = 0;
                for (DamageablePart part : this) {
                    float floatResult = ((float) part.initialMaxHealth) * globalFactor;
                    expectedNewMaxHealth += floatResult;
                    int result = (int) floatResult;
                    if (result % 2 == 1) {
                        int partMaxHealth = part.getMaxHealth();
                        if (part.getCurrentHealth() < partMaxHealth && reduced < 4) {
                            result--;
                            reduced++;
                        } else if (part.getCurrentHealth() > partMaxHealth && added < 4) {
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
                    newMaxHealth += result;
                    if (FirstAidConfig.debug) {
                        FirstAid.LOGGER.info("Part {} max health: {} initial; {} old; {} new", part.part.name(), part.initialMaxHealth, part.getMaxHealth(), result);
                    }
                    part.setMaxHealth(result);
                }
                entity.world.profiler.endStartSection("correcting");
                if (Math.abs(expectedNewMaxHealth - newMaxHealth) >= 2F) {
                    if (FirstAidConfig.debug) {
                        FirstAid.LOGGER.info("Entering second stage - diff {}", Math.abs(expectedNewMaxHealth - newMaxHealth));
                    }
                    List<DamageablePart> prioList = new ArrayList<>();
                    for (DamageablePart part : this) {
                        prioList.add(part);
                    }
                    prioList.sort(Comparator.comparingInt(DamageablePart::getMaxHealth));
                    for (DamageablePart part : prioList) {
                        int maxHealth = part.getMaxHealth();
                        if (FirstAidConfig.debug) {
                            FirstAid.LOGGER.info("Part {}: Second stage with total diff {}", part.part.name(), Math.abs(expectedNewMaxHealth - newMaxHealth));
                        }
                        if (expectedNewMaxHealth > newMaxHealth) {
                            part.setMaxHealth(maxHealth + 2);
                            newMaxHealth += (part.getMaxHealth() - maxHealth);
                        } else if (expectedNewMaxHealth < newMaxHealth) {
                            part.setMaxHealth(maxHealth - 2);
                            newMaxHealth -= (maxHealth - part.getMaxHealth());
                        }
                        if (Math.abs(expectedNewMaxHealth - newMaxHealth) < 2F) {
                            break;
                        }
                    }
                }
                entity.world.profiler.endSection();
            }
            prevScaleFactor = globalFactor;
            entity.world.profiler.endSection();
        }
    }

    @Override
    public boolean hasNoCritical() {
        return this.noCritical;
    }
}
