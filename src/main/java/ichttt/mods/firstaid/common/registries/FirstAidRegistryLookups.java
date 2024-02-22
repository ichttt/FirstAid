/*
 * FirstAid
 * Copyright (C) 2017-2024
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

package ichttt.mods.firstaid.common.registries;

import com.google.common.collect.ImmutableMap;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionTarget;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.common.util.LoggingMarkers;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.*;

public class FirstAidRegistryLookups {
    private static Map<DamageType, IDamageDistributionAlgorithm> DAMAGE_DISTRIBUTIONS;
    private static Map<EnumDebuffSlot, List<IDebuffBuilder>> DEBUFF_BUILDERS;

    public static IDamageDistributionAlgorithm getDamageDistributions(DamageType damageType) {
        return DAMAGE_DISTRIBUTIONS.get(damageType);
    }

    public static IDebuff[] getDebuffs(EnumDebuffSlot slot) {
        List<IDebuff> list = new ArrayList<>();
        for (IDebuffBuilder iDebuffBuilder : DEBUFF_BUILDERS.getOrDefault(slot, Collections.emptyList())) {
            IDebuff build = iDebuffBuilder.build();
            if (slot.playerParts.length > 1) {
                build = new SharedDebuff(build, slot);
            }
            list.add(build);
        }
        return list.toArray(new IDebuff[0]);
    }

    public static void init(RegistryAccess registryAccess) {
        DAMAGE_DISTRIBUTIONS = buildDamageDistributions(registryAccess);
        DEBUFF_BUILDERS = buildDebuffs(registryAccess);
        FirstAid.LOGGER.info(LoggingMarkers.REGISTRY, "Built FirstAid registry lookups");
    }

    private static Map<DamageType, IDamageDistributionAlgorithm> buildDamageDistributions(RegistryAccess registryAccess) {
        Registry<IDamageDistributionTarget> damageDistributionRegistry = registryAccess.registryOrThrow(FirstAidRegistries.Keys.DAMAGE_DISTRIBUTIONS);

        Map<DamageType, IDamageDistributionAlgorithm> staticAlgorithms = new HashMap<>();
        Map<DamageType, IDamageDistributionAlgorithm> dynamicAlgorithms = new HashMap<>();

        for (Map.Entry<ResourceKey<IDamageDistributionTarget>, IDamageDistributionTarget> entry : damageDistributionRegistry.entrySet()) {
            ResourceKey<IDamageDistributionTarget> key = entry.getKey();
            IDamageDistributionTarget distributionTarget = entry.getValue();

            IDamageDistributionAlgorithm algorithm = distributionTarget.getAlgorithm();
            List<DamageType> damageTypes = distributionTarget.buildApplyList(registryAccess.registryOrThrow(Registries.DAMAGE_TYPE));
            Map<DamageType, IDamageDistributionAlgorithm> mapToUse = distributionTarget.isDynamic() ? dynamicAlgorithms : staticAlgorithms;
            for (DamageType damageType : damageTypes) {
                IDamageDistributionAlgorithm oldVal = mapToUse.put(damageType, algorithm);
                if (oldVal != null) {
                    FirstAid.LOGGER.warn(LoggingMarkers.REGISTRY, "Damage distribution {} overwrites previously registered distribution for damage type {}", key, damageType.msgId());
                }
            }
        }
        ImmutableMap.Builder<DamageType, IDamageDistributionAlgorithm> allDamageDistributions = ImmutableMap.builder();
        allDamageDistributions.putAll(staticAlgorithms);
        for (Map.Entry<DamageType, IDamageDistributionAlgorithm> dynamicEntry : dynamicAlgorithms.entrySet()) {
            if (!staticAlgorithms.containsKey(dynamicEntry.getKey())) {
                allDamageDistributions.put(dynamicEntry);
            }
        }
        return allDamageDistributions.build();
    }

    private static Map<EnumDebuffSlot, List<IDebuffBuilder>> buildDebuffs(RegistryAccess registryAccess) {
        Registry<IDebuffBuilder> debuffBuilderRegistry = registryAccess.registryOrThrow(FirstAidRegistries.Keys.DEBUFFS);

        EnumMap<EnumDebuffSlot, List<IDebuffBuilder>> debuffMap = new EnumMap<>(EnumDebuffSlot.class);
        for (Map.Entry<ResourceKey<IDebuffBuilder>, IDebuffBuilder> entry : debuffBuilderRegistry.entrySet()) {
            ResourceKey<IDebuffBuilder> key = entry.getKey();
            IDebuffBuilder debuffBuilder = entry.getValue();
            debuffMap.computeIfAbsent(debuffBuilder.affectedSlot(), slot -> new ArrayList<>()).add(debuffBuilder);
        }
        return debuffMap;
    }

    public static void reset() {
        DAMAGE_DISTRIBUTIONS = null;
        DEBUFF_BUILDERS = null;
    }
}
