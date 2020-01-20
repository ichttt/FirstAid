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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.common.DataManagerWrapper;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.common.damagesystem.definition.DamageModelDefinition;
import ichttt.mods.firstaid.common.damagesystem.definition.DamageablePartDefinition;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EntityDamageModelImpl implements EntityDamageModel {

    protected final Set<SharedDebuff> sharedDebuffs;
    private final ImmutableCollection<DamageablePart> parts;
    private final DamageModelDefinition definition;
    private final Map<EntityEquipmentSlot, ImmutableList<DamageablePart>> map;
    protected int morphineTicksLeft = 0;
    protected final boolean noCritical;
    protected float prevHealthCurrent = -1F;
    protected float prevScaleFactor;
    protected boolean needsMorphineUpdate = false;

    public static EntityDamageModel create(DamageModelDefinition definition) {
        Map<EntityEquipmentSlot, ImmutableList<DamageablePart>> map = new HashMap<>();
        Set<SharedDebuff> sharedDebuffs = new HashSet<>();
        boolean noCritical = true;
        for (EntityEquipmentSlot slot : CommonUtils.ARMOR_SLOTS) {
            ImmutableList.Builder<DamageablePart> builder = new ImmutableList.Builder<>();
            Collection<DamageablePartDefinition> partDefinitions = definition.getPartDefinitions(slot);
            IDebuff[] debuffs = Objects.requireNonNull(FirstAidRegistry.getImpl()).getGeneralDebuff(slot, partDefinitions.size());
            if (debuffs.length > 0 && debuffs[0] instanceof SharedDebuff) {
                for (IDebuff debuff : debuffs)
                    sharedDebuffs.add((SharedDebuff) debuff);
            }
            for (DamageablePartDefinition partDefinition : partDefinitions) {
                if (!partDefinition.isCauseDeath()) noCritical = false;
                builder.add(new DamageablePartImpl(partDefinition.getName(), partDefinition.getMaxHealth(), partDefinition.isCauseDeath(), debuffs));
            }
            map.put(slot, builder.build());
        }
        return new EntityDamageModelImpl(definition, map, noCritical, sharedDebuffs);
    }

    protected EntityDamageModelImpl(DamageModelDefinition definition, Map<EntityEquipmentSlot, ImmutableList<DamageablePart>> map, boolean noCritical, Set<SharedDebuff> debuffs) {
        this.definition = definition;
        this.map = map;
        ImmutableList.Builder<DamageablePart> builder = ImmutableList.builder();
        for (EntityEquipmentSlot slot : CommonUtils.ARMOR_SLOTS) {
            builder.addAll(map.get(slot));
        }
        this.parts = builder.build();
        this.noCritical = noCritical;
        this.sharedDebuffs = debuffs;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        for (DamageablePart part : getParts())
            tagCompound.setTag(part.getName(), part.serializeNBT());
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        for (DamageablePart part : getParts())
            part.deserializeNBT(nbt.getCompoundTag(part.getName()));
    }

    @Override
    public boolean tick(World world, EntityLivingBase entity) {
        if (isDead(entity))
            return false;
        world.profiler.startSection("FirstAidDamageModel");

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
        getParts().forEach(part -> part.tick(world, entity, morphine == null));
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
    public float getCurrentHealth() {
        float currentHealth = 0;
        for (DamageablePart part : this.getParts())
            currentHealth += part.getCurrentHealth();
        return currentHealth;
    }

    @Override
    public boolean isDead(@Nullable EntityLivingBase entity) {
        if (entity != null && (entity.isDead || entity.getHealth() <= 0F))
            return true;

        if (this.noCritical) {
            boolean dead = true;
            for (DamageablePart part : this.getParts()) {
                if (part.getCurrentHealth() > 0) {
                    dead = false;
                    break;
                }
            }
            return dead;
        } else {
            for (DamageablePart part : this.getParts()) {
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
        for (DamageablePart part : this.getParts())
                value += part.getAbsorption();
        return value; //Autoboxing FTW
    }

    @Override
    public void setAbsorption(float absorption) {
        final float newAbsorption = absorption / 8F;
        getParts().forEach(damageablePart -> damageablePart.setAbsorption(newAbsorption));
    }

    @Override
    public int getCurrentMaxHealth() {
        int maxHealth = 0;
        for (DamageablePart part : this.getParts()) {
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
                for (DamageablePart part : this.getParts()) {
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
                        FirstAid.LOGGER.info("Part {} max health: {} initial; {} old; {} new", part.getName(), part.initialMaxHealth, part.getMaxHealth(), result);
                    }
                    part.setMaxHealth(result);
                }
                entity.world.profiler.endStartSection("correcting");
                if (Math.abs(expectedNewMaxHealth - newMaxHealth) >= 2F) {
                    if (FirstAidConfig.debug) {
                        FirstAid.LOGGER.info("Entering second stage - diff {}", Math.abs(expectedNewMaxHealth - newMaxHealth));
                    }
                    List<DamageablePart> prioList = new ArrayList<>(this.getParts());
                    prioList.sort(Comparator.comparingInt(DamageablePart::getMaxHealth));
                    for (DamageablePart part : prioList) {
                        int maxHealth = part.getMaxHealth();
                        if (FirstAidConfig.debug) {
                            FirstAid.LOGGER.info("Part {}: Second stage with total diff {}", part.getName(), Math.abs(expectedNewMaxHealth - newMaxHealth));
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

    @Override
    public ImmutableCollection<DamageablePart> getParts() {
        return this.parts;
    }

    @Override
    public ImmutableList<DamageablePart> getParts(EntityEquipmentSlot slot) {
        return this.map.get(slot);
    }

    @Override
    public EntityDamageModel createCopy() {
        return create(definition);
    }

    @Nullable
    @Override
    public EntityEquipmentSlot getHitSlot(float yRelative) {
        Map<EnumHeightOffset, Float> offsets = definition.getHeightOffsets();
        if (offsets == null) return null;
        if (yRelative > offsets.get(EnumHeightOffset.HEAD_BODY))
            return EntityEquipmentSlot.HEAD;
        else if (yRelative > offsets.get(EnumHeightOffset.BODY_LEGS))
            return EntityEquipmentSlot.CHEST;
        else if (yRelative > offsets.get(EnumHeightOffset.LEGS_FEET))
            return EntityEquipmentSlot.LEGS;
        else
            return EntityEquipmentSlot.FEET;
    }
}
