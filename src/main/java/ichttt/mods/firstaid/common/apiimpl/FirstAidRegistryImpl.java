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

package ichttt.mods.firstaid.common.apiimpl;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.PartHealer;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumPlayerDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.debuff.ConstantDebuff;
import ichttt.mods.firstaid.common.damagesystem.debuff.OnHitDebuff;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.common.damagesystem.distribution.GeneralDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.PlayerDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistribution;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FirstAidRegistryImpl extends FirstAidRegistry {
    public static final FirstAidRegistryImpl INSTANCE = new FirstAidRegistryImpl();
    private final Map<String, IDamageDistribution> DISTRIBUTION_MAP = new HashMap<>();
    private final Map<Item, Pair<Function<ItemStack, PartHealer>, Function<ItemStack, Integer>>> HEALER_MAP = new HashMap<>();
    private final Multimap<EnumPlayerDebuffSlot, Supplier<IDebuff>> PLAYER_DEBUFFS = HashMultimap.create();
    private final Multimap<EntityEquipmentSlot, Supplier<IDebuff>> GENERAL_DEBUFFS = HashMultimap.create();
    private boolean registrationAllowed = true;

    public static void finish() {
        FirstAidRegistry registryImpl = FirstAidRegistry.getImpl();
        if (registryImpl == null)
            throw new IllegalStateException("The apiimpl has not been set! Something went seriously wrong!");
        if (registryImpl != INSTANCE)
            throw new IllegalStateException("A mod has registered a custom apiimpl for the registry. THIS IS NOT ALLOWED!" +
            "It should be " + INSTANCE.getClass().getName() + " but it actually is " + registryImpl.getClass().getName());
        INSTANCE.registrationAllowed = false;
        if (FirstAidConfig.debug) {
            FirstAid.LOGGER.info("REG READOUT:");
            for (Map.Entry<String, IDamageDistribution> entry : INSTANCE.DISTRIBUTION_MAP.entrySet()) {
                FirstAid.LOGGER.info("{} bound to {}", entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void bindDamageSourceStandard(@Nonnull DamageSource damageType, boolean shufflePriorityTable, @Nonnull EntityEquipmentSlot... priorityList) {
        this.bindDamageSourceStandard(damageType, shufflePriorityTable, null, priorityList);
    }

    @Override
    public void bindDamageSourceStandard(@Nonnull DamageSource damageType, boolean shufflePriorityTable, @Nullable List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> priorityTable, @Nonnull EntityEquipmentSlot... priorityList) {
        if (priorityTable == null)
            this.bindDamageSourceCustom(damageType, new GeneralDamageDistribution(shufflePriorityTable, priorityList));
        else
            this.bindDamageSourceCustom(damageType, new StandardDamageDistribution(new PlayerDamageDistribution(shufflePriorityTable, priorityTable), new GeneralDamageDistribution(shufflePriorityTable, priorityList)));
    }

    @Override
    public void bindDamageSourceRandom(@Nonnull DamageSource damageType, boolean nearestFirst, boolean tryNoKill) {
        if (nearestFirst) {
            if (!tryNoKill)
                DISTRIBUTION_MAP.remove(damageType.damageType);
            else
                bindDamageSourceCustom(damageType, RandomDamageDistribution.NEAREST_NOKILL);
        } else {
            bindDamageSourceCustom(damageType, tryNoKill ? RandomDamageDistribution.ANY_NOKILL : RandomDamageDistribution.ANY_KILL);
        }
    }

    @Override
    public void bindDamageSourceCustom(@Nonnull DamageSource damageType, @Nonnull IDamageDistribution distributionTable) {
        if (DISTRIBUTION_MAP.containsKey(damageType.damageType))
            FirstAid.LOGGER.info("Damage Distribution override detected for source " + damageType);
        DISTRIBUTION_MAP.put(damageType.damageType, distributionTable);
    }

    @Override
    public void registerHealingType(@Nonnull Item item, @Nonnull Function<ItemStack, PartHealer> factory, Function<ItemStack, Integer> applyTime) {
        if (this.HEALER_MAP.containsKey(item))
            FirstAid.LOGGER.warn("Healing type override detected for item " + item);
        this.HEALER_MAP.put(item, Pair.of(factory, applyTime));
    }

    @Nullable
    @Override
    public PartHealer getPartHealer(@Nonnull ItemStack type) {
        Pair<Function<ItemStack, PartHealer>, Function<ItemStack, Integer>> pair = this.HEALER_MAP.get(type.getItem());
        if (pair != null)
            return pair.getLeft().apply(type);
        return null;
    }

    @Override
    public Integer getPartHealingTime(@Nonnull ItemStack stack) {
        Pair<Function<ItemStack, PartHealer>, Function<ItemStack, Integer>> pair = this.HEALER_MAP.get(stack.getItem());
        if (pair != null)
            return pair.getRight().apply(stack);
        return null;
    }

    @Override
    public void registerDebuff(@Nonnull EnumPlayerDebuffSlot playerSlot, @Nullable EntityEquipmentSlot generalSlot, @Nonnull IDebuffBuilder abstractBuilder) {
        DebuffBuilder builder;
        try {
            builder = (DebuffBuilder) abstractBuilder;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Builder must an instance of the default builder received via DebuffBuilderFactory!", e);
        }
        //Build the finished debuff
        FirstAid.LOGGER.debug("Building debuff from mod {} for slot {} (general {}) with potion effect {}, type = {}", CommonUtils.getActiveModidSafe(), playerSlot, generalSlot, builder.potionName, builder.isOnHit ? "OnHit" : "Constant");
        BooleanSupplier isEnabled;
        if (builder.isEnabledSupplier == null)
            isEnabled = () -> true;
        else
            isEnabled = builder.isEnabledSupplier;

        Preconditions.checkArgument(!builder.map.isEmpty(), "Failed to register debuff with condition has set");
        Supplier<IDebuff> debuff;
        if (builder.isOnHit) {
            debuff = () -> new OnHitDebuff(builder.potionName, builder.map, isEnabled, builder.sound);
        } else {
            Preconditions.checkArgument(builder.sound == null, "Tried to register constant debuff with sound effect.");
            debuff = () -> new ConstantDebuff(builder.potionName, builder.map, isEnabled);
        }
        registerDebuff(playerSlot, generalSlot, debuff);
    }

    @Override
    public void registerDebuff(@Nonnull EnumPlayerDebuffSlot playerSlot, @Nullable EntityEquipmentSlot generalSlot, @Nonnull Supplier<IDebuff> debuff) {
        if (!registrationAllowed)
            throw new IllegalStateException("Registration must take place before FMLLoadCompleteEvent");

        if (debuff instanceof SharedDebuff) {
            throw new IllegalArgumentException("Debuff must not be shared!");
        }

        this.PLAYER_DEBUFFS.put(playerSlot, playerSlot.playerParts.length <= 1 ? debuff : () -> new SharedDebuff(debuff.get(), playerSlot.playerParts.length));
    }

    @Nonnull
    @Override
    public IDamageDistribution getDamageDistribution(@Nonnull DamageSource source) {
        IDamageDistribution distribution = DISTRIBUTION_MAP.get(source.damageType);
        if (distribution == null)
            distribution = RandomDamageDistribution.NEAREST_KILL;
        return distribution;
    }

    @Nonnull
    @Override
    public IDebuff[] getPlayerDebuff(@Nonnull EnumPlayerDebuffSlot slot) {
        return PLAYER_DEBUFFS.get(slot).stream().map(Supplier::get).toArray(IDebuff[]::new);
    }

    @Nonnull
    @Override
    public IDebuff[] getGeneralDebuff(@Nonnull EntityEquipmentSlot slot, int count) {
        Stream<IDebuff> stream =  GENERAL_DEBUFFS.get(slot).stream().map(Supplier::get);
        if (count > 1)
            stream = stream.map(iDebuff -> new SharedDebuff(iDebuff, count));
        return stream.toArray(IDebuff[]::new);
    }
}
