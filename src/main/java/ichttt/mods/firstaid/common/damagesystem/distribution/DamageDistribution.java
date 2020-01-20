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

package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import ichttt.mods.firstaid.api.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent;
import ichttt.mods.firstaid.api.event.FirstAidPlayerDamageEvent;
import ichttt.mods.firstaid.common.network.MessageUpdatePart;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class DamageDistribution implements IDamageDistribution {

    public static float handleDamageTaken(IDamageDistribution damageDistribution, EntityDamageModel damageModel, float damage, @Nonnull EntityLivingBase entity, @Nonnull DamageSource source, boolean addStat, boolean redistributeIfLeft) {
        if (FirstAidConfig.debug) {
            FirstAid.LOGGER.info("Damaging {} using {} for dmg source {}, redistribute {}, addStat {}", damage, damageDistribution.toString(), source.damageType, redistributeIfLeft, addStat);
        }
        NBTTagCompound beforeCache = damageModel.serializeNBT();
        damage = ArmorUtils.applyGlobalPotionModifiers(entity, source, damage);
        //VANILLA COPY - combat tracker and exhaustion
        if (damage != 0.0F) {
            if (entity instanceof EntityPlayer)
                ((EntityPlayer) entity).addExhaustion(source.getHungerDamage());
            float currentHealth = entity.getHealth();
            entity.getCombatTracker().trackDamage(source, currentHealth, damage);
        }

        float left = damageDistribution.distributeDamage(damage, entity, source, addStat);
        if (left > 0 && redistributeIfLeft) {
            damageDistribution = RandomDamageDistribution.NEAREST_KILL;
            left = damageDistribution.distributeDamage(left, entity, source, addStat);
        }

        FirstAidLivingDamageEvent event;
        if (entity instanceof EntityPlayer) {
            PlayerDamageModel before = (PlayerDamageModel) damageModel.createCopy();
            before.deserializeNBT(beforeCache);
            event = new FirstAidPlayerDamageEvent((EntityPlayer) entity, (PlayerDamageModel) damageModel, before, source, left);

        } else {
            EntityDamageModel before = damageModel.createCopy();
            before.deserializeNBT(beforeCache);
            event = new FirstAidLivingDamageEvent(entity, damageModel, before, source, left);
        }
        if (MinecraftForge.EVENT_BUS.post(event)) {
            damageModel.deserializeNBT(beforeCache); //restore prev state
            return 0F;
        }


        if (damageModel.isDead(entity))
            CommonUtils.killEntity(entity, source);
        return left;
    }

    protected float minHealth(@Nonnull EntityLivingBase entity, @Nonnull DamageablePart part) {
        return 0F;
    }

    protected float distributeDamageOnParts(float damage, @Nonnull EntityDamageModel damageModel, @Nonnull List<DamageablePart> damageableParts, @Nonnull EntityLivingBase entity, boolean addStat) {
        if (damageableParts.size() > 1) {
            damageableParts = new ArrayList<>(damageableParts);
            Collections.shuffle(damageableParts);
        }
        for (DamageablePart part : damageableParts) {
            float minHealth = minHealth(entity, part);
            float dmgDone = damage - part.damage(damage, entity, damageModel.getMorphineTicks() == 0, minHealth);
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                FirstAid.NETWORKING.sendTo(new MessageUpdatePart(part, EnumPlayerPart.fromPart(part)), (EntityPlayerMP) player);
                if (addStat)
                    player.addStat(StatList.DAMAGE_TAKEN, Math.round(dmgDone * 10.0F));
            }
            damage -= dmgDone;
            if (damage == 0)
                break;
            else if (damage < 0) {
                FirstAid.LOGGER.error("Got negative damage {} left? Logic error? ", damage);
                break;
            }
        }
        return damage;
    }

    @Nonnull
    protected abstract List<Pair<EntityEquipmentSlot, List<DamageablePart>>> getPartList(EntityDamageModel damageModel, EntityLivingBase entity);

    @Override
    public float distributeDamage(float damage, @Nonnull EntityLivingBase entity, @Nonnull DamageSource source, boolean addStat) {
        EntityDamageModel damageModel = Objects.requireNonNull(entity.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
        for (Pair<EntityEquipmentSlot, List<DamageablePart>> pair : getPartList(damageModel, entity)) {
            EntityEquipmentSlot slot = pair.getLeft();
            damage = ArmorUtils.applyArmor(entity, entity.getItemStackFromSlot(slot), source, damage, slot);
            if (damage <= 0F)
                return 0F;
            damage = ArmorUtils.applyEnchantmentModifiers(entity.getItemStackFromSlot(slot), source, damage);
            if (damage <= 0F)
                return 0F;
            damage = ForgeHooks.onLivingDamage(entity, source, damage); //we post every time we damage a part, make it so other mods can modify
            if (damage <= 0F) return 0F;

            damage = distributeDamageOnParts(damage, damageModel, pair.getRight(), entity, addStat);
            if (damage == 0F)
                break;
        }
        return damage;
    }
}
