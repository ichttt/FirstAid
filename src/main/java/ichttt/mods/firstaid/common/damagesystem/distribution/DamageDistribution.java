/*
 * FirstAid
 * Copyright (C) 2017-2022
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class DamageDistribution implements IDamageDistribution {

    public static float handleDamageTaken(IDamageDistribution damageDistribution, AbstractPlayerDamageModel damageModel, float damage, @Nonnull Player player, @Nonnull DamageSource source, boolean addStat, boolean redistributeIfLeft) {
        if (FirstAidConfig.GENERAL.debug.get()) {
            FirstAid.LOGGER.info("--- Damaging {} using {} for dmg source {}, redistribute {}, addStat {} ---", damage, damageDistribution.toString(), source.msgId, redistributeIfLeft, addStat);
        }
        CompoundTag beforeCache = damageModel.serializeNBT();
        if (!damageDistribution.skipGlobalPotionModifiers())
            damage = ArmorUtils.applyGlobalPotionModifiers(player, source, damage);
        //VANILLA COPY - combat tracker and exhaustion
        if (damage != 0.0F) {
            player.causeFoodExhaustion(source.getFoodExhaustion());
            float currentHealth = player.getHealth();
            player.getCombatTracker().recordDamage(source, currentHealth, damage);
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
            if (FirstAidConfig.GENERAL.debug.get()) {
                FirstAid.LOGGER.info("--- DONE! Event got canceled ---");
            }
            return 0F;
        }

        if (damageModel.isDead(player))
            CommonUtils.killPlayer(damageModel, player, source);
        if (FirstAidConfig.GENERAL.debug.get()) {
            FirstAid.LOGGER.info("--- DONE! {} still left ---", left);
        }
        return left;
    }

    protected float minHealth(@Nonnull Player player, @Nonnull AbstractDamageablePart part) {
        return 0F;
    }

    protected float distributeDamageOnParts(float damage, @Nonnull AbstractPlayerDamageModel damageModel, @Nonnull EnumPlayerPart[] enumParts, @Nonnull Player player, boolean addStat) {
        ArrayList<AbstractDamageablePart> damageableParts = new ArrayList<>(enumParts.length);
        for (EnumPlayerPart part : enumParts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }
        Collections.shuffle(damageableParts);
        for (AbstractDamageablePart part : damageableParts) {
            float minHealth = minHealth(player, part);
            float dmgDone = damage - part.damage(damage, player, !player.hasEffect(EventHandler.MORPHINE), minHealth);
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new MessageUpdatePart(part));
            if (addStat)
                player.awardStat(Stats.DAMAGE_TAKEN, Math.round(dmgDone * 10.0F));
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
    protected abstract List<Pair<EquipmentSlot, EnumPlayerPart[]>> getPartList();

    @Override
    public float distributeDamage(float damage, @Nonnull Player player, @Nonnull DamageSource source, boolean addStat) {
        if (damage <= 0F) return 0F;
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        if (FirstAidConfig.GENERAL.debug.get()) {
            FirstAid.LOGGER.info("Starting distribution of {} damage...", damage);
        }
        for (Pair<EquipmentSlot, EnumPlayerPart[]> pair : getPartList()) {
            EquipmentSlot slot = pair.getLeft();
            EnumPlayerPart[] parts = pair.getRight();
            if (Arrays.stream(parts).map(damageModel::getFromEnum).anyMatch(part -> part.currentHealth > minHealth(player, part))) {
                final float originalDamage = damage;
                damage = ArmorUtils.applyArmor(player, player.getItemBySlot(slot), source, damage, slot);
                if (damage <= 0F)
                    return 0F;
                damage = ArmorUtils.applyEnchantmentModifiers(player, slot, source, damage);
                if (damage <= 0F)
                    return 0F;
                damage = ForgeHooks.onLivingDamage(player, source, damage); //we post every time we damage a part, make it so other mods can modify
                if (damage <= 0F) return 0F;
                final float dmgAfterReduce = damage;

                damage = distributeDamageOnParts(damage, damageModel, parts, player, addStat);
                if (damage == 0F)
                    break;
                final float absorbFactor = originalDamage / dmgAfterReduce;
                final float damageDistributed = dmgAfterReduce - damage;
                damage = originalDamage - (damageDistributed * absorbFactor);
                if (FirstAidConfig.GENERAL.debug.get()) {
                    FirstAid.LOGGER.info("Distribution round: Not done yet, going to next round. Needed to distribute {} damage (reduced to {}) to {}, but only distributed {}. New damage to be distributed is {}, based on absorb factor {}", originalDamage, dmgAfterReduce, slot, damageDistributed, damage, absorbFactor);
                }
            } else if (FirstAidConfig.GENERAL.debug.get()) {
                FirstAid.LOGGER.info("Skipping {}, no health > min in parts!", slot);
            }
        }
        return damage;
    }
}
