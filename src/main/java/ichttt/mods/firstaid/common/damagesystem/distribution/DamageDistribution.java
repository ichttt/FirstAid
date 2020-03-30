/*
 * FirstAid
 * Copyright (C) 2017-2020
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
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.network.MessageUpdatePart;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import ichttt.mods.firstaid.common.util.CommonUtils;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class DamageDistribution implements IDamageDistribution {

    public static float handleDamageTaken(IDamageDistribution damageDistribution, AbstractPlayerDamageModel damageModel, float damage, @Nonnull EntityPlayer player, @Nonnull DamageSource source, boolean addStat, boolean redistributeIfLeft) {
        if (FirstAidConfig.debug) {
            FirstAid.LOGGER.info("Damaging {} using {} for dmg source {}, redistribute {}, addStat {}", damage, damageDistribution.toString(), source.damageType, redistributeIfLeft, addStat);
        }
        NBTTagCompound beforeCache = damageModel.serializeNBT();
        if (!damageDistribution.skipGlobalPotionModifiers())
            damage = ArmorUtils.applyGlobalPotionModifiers(player, source, damage);
        //VANILLA COPY - combat tracker and exhaustion
        if (damage != 0.0F) {
            player.addExhaustion(source.getHungerDamage());
            float currentHealth = player.getHealth();
            player.getCombatTracker().trackDamage(source, currentHealth, damage);
        }

        float left = damageDistribution.distributeDamage(damage, player, source, addStat);
        if (left > 0 && redistributeIfLeft) {
            boolean hasTriedNoKill = damageDistribution == RandomDamageDistribution.NEAREST_NOKILL || damageDistribution == RandomDamageDistribution.ANY_NOKILL;
            damageDistribution = hasTriedNoKill ? RandomDamageDistribution.NEAREST_KILL : RandomDamageDistribution.getDefault();
            left = damageDistribution.distributeDamage(left, player, source, addStat);
            if (left > 0 && !hasTriedNoKill) {
                damageDistribution = RandomDamageDistribution.NEAREST_KILL;
                left = damageDistribution.distributeDamage(left, player, source, addStat);
            }
        }
        PlayerDamageModel before = PlayerDamageModel.create();
        before.deserializeNBT(beforeCache);
        if (MinecraftForge.EVENT_BUS.post(new FirstAidLivingDamageEvent(player, damageModel, before, source, left))) {
            damageModel.deserializeNBT(beforeCache); //restore prev state
            return 0F;
        }

        if (damageModel.isDead(player))
            CommonUtils.killPlayer(damageModel, player, source);
        return left;
    }

    protected float minHealth(@Nonnull EntityPlayer player, @Nonnull AbstractDamageablePart part) {
        return 0F;
    }

    protected float distributeDamageOnParts(float damage, @Nonnull AbstractPlayerDamageModel damageModel, @Nonnull EnumPlayerPart[] enumParts, @Nonnull EntityPlayer player, boolean addStat) {
        ArrayList<AbstractDamageablePart> damageableParts = new ArrayList<>(enumParts.length);
        for (EnumPlayerPart part : enumParts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }
        Collections.shuffle(damageableParts);
        for (AbstractDamageablePart part : damageableParts) {
            float minHealth = minHealth(player, part);
            float dmgDone = damage - part.damage(damage, player, !player.isPotionActive(EventHandler.MORPHINE), minHealth);
            FirstAid.NETWORKING.sendTo(new MessageUpdatePart(part), (EntityPlayerMP) player);
            if (addStat)
                player.addStat(StatList.DAMAGE_TAKEN, Math.round(dmgDone * 10.0F));
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
    protected abstract List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList();

    @Override
    public float distributeDamage(float damage, @Nonnull EntityPlayer player, @Nonnull DamageSource source, boolean addStat) {
        AbstractPlayerDamageModel damageModel = Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
        for (Pair<EntityEquipmentSlot, EnumPlayerPart[]> pair : getPartList()) {
            EntityEquipmentSlot slot = pair.getLeft();
            EnumPlayerPart[] parts = pair.getRight();
            if (Arrays.stream(parts).map(damageModel::getFromEnum).anyMatch(part -> part.currentHealth > minHealth(player, part))) {
                damage = ArmorUtils.applyArmor(player, player.getItemStackFromSlot(slot), source, damage, slot);
                if (damage <= 0F)
                    return 0F;
                damage = ArmorUtils.applyEnchantmentModifiers(player.getItemStackFromSlot(slot), source, damage);
                if (damage <= 0F)
                    return 0F;
                damage = ForgeHooks.onLivingDamage(player, source, damage); //we post every time we damage a part, make it so other mods can modify
                if (damage <= 0F) return 0F;

                damage = distributeDamageOnParts(damage, damageModel, parts, player, addStat);
                if (damage == 0F)
                    break;
            } else if (FirstAidConfig.debug) {
                FirstAid.LOGGER.info("Skipping {}, no health <in parts!", slot);
            }
        }
        return damage;
    }
}
