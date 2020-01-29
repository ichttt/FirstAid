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

package ichttt.mods.firstaid.common.apiimpl;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.debuff.builder.IDebuffBuilder;
import ichttt.mods.firstaid.api.distribution.IRandomDamageDistributionBuilder;
import ichttt.mods.firstaid.api.distribution.IStandardDamageDistributionBuilder;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.apiimpl.distribution.DamageDistributionBuilderFactoryImpl;
import ichttt.mods.firstaid.common.damagesystem.debuff.ConstantDebuff;
import ichttt.mods.firstaid.common.damagesystem.debuff.OnHitDebuff;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FirstAidRegistryImpl extends FirstAidRegistry {
    public static final FirstAidRegistryImpl INSTANCE = new FirstAidRegistryImpl();
    private final ArrayList<Pair<Predicate<DamageSource>, IDamageDistribution>> distributionsDynamic = new ArrayList<>();
    private final Map<String, IDamageDistribution> distributionsStatic = new HashMap<>();
    private final Map<Item, Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>>> healerMap = new HashMap<>();
    private final Multimap<EnumDebuffSlot, Supplier<IDebuff>> debuffs = HashMultimap.create();
    private boolean registrationAllowed = true;

    public static void finish() {
        FirstAidRegistry registryImpl = FirstAidRegistry.getImpl();
        if (registryImpl == null)
            throw new IllegalStateException("The apiimpl has not been set! Something went seriously wrong!");
        if (registryImpl != INSTANCE)
            throw new IllegalStateException("A mod has registered a custom apiimpl for the registry. THIS IS NOT ALLOWED!" +
            "It should be " + INSTANCE.getClass().getName() + " but it actually is " + registryImpl.getClass().getName());
        INSTANCE.registrationAllowed = false;
        INSTANCE.distributionsDynamic.trimToSize();
        if (FirstAidConfig.debug) {
            FirstAid.LOGGER.info("REG READOUT:");
            for (Map.Entry<String, IDamageDistribution> entry : INSTANCE.distributionsStatic.entrySet()) {
                FirstAid.LOGGER.info("{} bound to {}", entry.getKey(), entry.getValue());
            }
            FirstAid.LOGGER.info("+{} additional dynamic distributions", INSTANCE.distributionsDynamic.size());
        }
    }

    @Deprecated
    @Override
    public void bindDamageSourceStandard(@Nonnull DamageSource damageType, @Nonnull List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> priorityTable, boolean shufflePriorityTable) {
        IStandardDamageDistributionBuilder builder = DamageDistributionBuilderFactoryImpl.INSTANCE.newStandardBuilder();
        for (Pair<EntityEquipmentSlot, EnumPlayerPart[]> pair : priorityTable)
            builder.addDistributionLayer(pair.getLeft(), pair.getRight());
        if (shufflePriorityTable)
            builder.ignoreOrder();
        builder.registerStatic(damageType);
    }

    @Deprecated
    @Override
    public void bindDamageSourceRandom(@Nonnull DamageSource damageType, boolean nearestFirst, boolean tryNoKill) {
        IRandomDamageDistributionBuilder builder = DamageDistributionBuilderFactoryImpl.INSTANCE.newRandomBuilder();
        if (nearestFirst)
            builder.useNearestFirst();
        if (tryNoKill)
            builder.tryNoKill();
        builder.registerStatic(damageType);
    }

    @Deprecated
    @Override
    public void bindDamageSourceCustom(@Nonnull DamageSource damageType, @Nonnull IDamageDistribution distributionTable) {
        DamageDistributionBuilderFactoryImpl.INSTANCE.newCustomBuilder(distributionTable).registerStatic(damageType);
    }

    @Override
    public void registerHealingType(@Nonnull Item item, @Nonnull Function<ItemStack, AbstractPartHealer> factory, Function<ItemStack, Integer> applyTime) {
        if (this.healerMap.containsKey(item))
            FirstAid.LOGGER.warn("Healing type override detected for item " + item);
        this.healerMap.put(item, Pair.of(factory, applyTime));
    }

    @Nullable
    @Override
    public AbstractPartHealer getPartHealer(@Nonnull ItemStack type) {
        Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>> pair = this.healerMap.get(type.getItem());
        if (pair != null)
            return pair.getLeft().apply(type);
        return null;
    }

    @Override
    public Integer getPartHealingTime(@Nonnull ItemStack stack) {
        Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>> pair = this.healerMap.get(stack.getItem());
        if (pair != null)
            return pair.getRight().apply(stack);
        return null;
    }

    @Override
    public void registerDebuff(@Nonnull EnumDebuffSlot slot, @Nonnull IDebuffBuilder abstractBuilder) {
        DebuffBuilder builder;
        try {
            builder = (DebuffBuilder) abstractBuilder;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Builder must an instance of the default builder received via DebuffBuilderFactory!", e);
        }
        //Build the finished debuff
        FirstAid.LOGGER.debug("Building debuff from mod {} for slot {} with potion effect {}, type = {}", CommonUtils.getActiveModidSafe(), slot, builder.potionName, builder.isOnHit ? "OnHit" : "Constant");
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
        registerDebuff(slot, debuff);
    }

    @Override
    public void registerDebuff(@Nonnull EnumDebuffSlot slot, @Nonnull Supplier<IDebuff> debuff) {
        if (!registrationAllowed)
            throw new IllegalStateException("Registration must take place before FMLLoadCompleteEvent");

        if (slot.playerParts.length > 1 && !(debuff instanceof SharedDebuff)) {
            this.debuffs.put(slot, () -> new SharedDebuff(debuff.get(), slot));
            return;
        }

        this.debuffs.put(slot, debuff);
    }

    @Nonnull
    @Override
    public IDamageDistribution getDamageDistribution(@Nonnull DamageSource source) {
        IDamageDistribution distribution = distributionsStatic.get(source.damageType);
        if (distribution == null) {
            //lookup if we have any matching dynamic distribution
            for (Pair<Predicate<DamageSource>, IDamageDistribution> pair : distributionsDynamic) {
                if (pair.getLeft().test(source)) {
                    distribution = pair.getRight();
                    break;
                }
            }
        }
        if (distribution == null)
            distribution = RandomDamageDistribution.getDefault();
        return distribution;
    }

    @Nonnull
    @Override
    public IDebuff[] getDebuffs(@Nonnull EnumDebuffSlot slot) {
        return debuffs.get(slot).stream().map(Supplier::get).toArray(IDebuff[]::new);
    }

    public void registerDistribution(Predicate<DamageSource> matcher, IDamageDistribution distribution) {
        if (!registrationAllowed) throw new IllegalStateException("Too late to register!");
        distributionsDynamic.add(Pair.of(matcher, distribution));
    }

    public void registerDistribution(DamageSource[] sources, IDamageDistribution distribution) {
        if (!registrationAllowed) throw new IllegalStateException("Too late to register!");
        for (DamageSource damageType : sources) {
            if (distributionsStatic.containsKey(damageType.damageType))
                FirstAid.LOGGER.info("Damage Distribution override detected for source " + damageType);
            distributionsStatic.put(damageType.damageType, distribution);
        }
    }
}
