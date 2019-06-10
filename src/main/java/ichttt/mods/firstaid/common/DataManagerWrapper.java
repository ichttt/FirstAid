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

package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.network.MessageApplyAbsorption;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 * This is a hack to intervene all calls to absorption. It's not optimal but it's the best I could come up with without a coremod
 * This should be compatible with other mods which do so as I respect the parent in any other case.
 */
public class DataManagerWrapper extends EntityDataManager {
    private final PlayerEntity player;
    private final EntityDataManager parent;

    public DataManagerWrapper(PlayerEntity player, EntityDataManager parent) {
        super(player);
        this.player = player;
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public <T> T get(@Nonnull DataParameter<T> key) {
        if (key == PlayerEntity.ABSORPTION && player.isAlive())
            parent.set(key, (T) CommonUtils.getDamageModel(player).getAbsorption());
        return parent.get(key);
    }

    public <T> void set_impl(@Nonnull DataParameter<T> key, @Nonnull T value) {
        parent.set(key, value);
    }

    @Override
    public <T> void set(@Nonnull DataParameter<T> key, @Nonnull T value) {
        if (key == PlayerEntity.ABSORPTION) {
            float floatValue = (Float) value;
            if (player instanceof ServerPlayerEntity) { //may be EntityOtherPlayerMP as well
                ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
                if (playerMP.connection != null) //also fired when connecting, ignore(otherwise the net handler would crash)
                    FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> playerMP), new MessageApplyAbsorption(floatValue));
            }
            CommonUtils.getDamageModel(player).setAbsorption(floatValue);
        } else if (key == LivingEntity.HEALTH) {
            if (value instanceof Float) {
                Float aFloat = (Float) value;
                LazyOptional<AbstractPlayerDamageModel> damageModel;
                if (aFloat > player.getMaxHealth()) {
                    CommonUtils.getDamageModel(player).forEach(damageablePart -> damageablePart.currentHealth = damageablePart.getMaxHealth());
                } else if ((damageModel = player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)).isPresent() && damageModel.orElseThrow(RuntimeException::new).isWaitingForHelp()) {
                    if (FirstAidConfig.debug)
                        CommonUtils.debugLogStacktrace("SetHealth falltrough");
                } else if (FirstAidConfig.watchSetHealth && !aFloat.isInfinite() && !aFloat.isNaN() && aFloat > 0 && !player.world.isRemote && player instanceof ServerPlayerEntity && ((ServerPlayerEntity) player).connection != null) {
                    //calculate diff
                    Float orig = get(LivingEntity.HEALTH);
                    if (orig > 0 && !orig.isNaN() && !orig.isInfinite()) {
                        float healed = aFloat - orig;
                        if (Math.abs(healed) > 0.001) {
                            if (healed < 0) {
                                if (FirstAidConfig.debug) {
                                    CommonUtils.debugLogStacktrace("DAMAGING: " + (-healed));
                                }
                                DamageDistribution.handleDamageTaken(RandomDamageDistribution.NEAREST_KILL, CommonUtils.getDamageModel(player), -healed, player, DamageSource.MAGIC, true, true);
                            } else {
                                if (FirstAidConfig.debug) {
                                    CommonUtils.debugLogStacktrace("HEALING: " + healed);
                                }
                                HealthDistribution.addRandomHealth(aFloat, player, true);
                            }
                        }
                        return;
                    }
                }
            }
        }
        set_impl(key, value);
    }

    // ----------WRAPPER BELOW----------

    @Override
    public boolean isDirty() {
        return parent.isDirty();
    }

    @Override
    @Nullable
    public List<DataEntry<?>> getDirty() {
        return parent.getDirty();
    }

    @Override
    public void writeEntries(PacketBuffer buf) throws IOException {
        parent.writeEntries(buf);
    }

    @Override
    @Nullable
    public List<DataEntry<?>> getAll() {
        return parent.getAll();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setEntryValues(List<DataEntry<?>> entriesIn) {
        parent.setEntryValues(entriesIn);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T> void setEntryValue(DataEntry<T> target, DataEntry<?> source) {
        parent.setEntryValue(target, source);
    }

    @Override
    public boolean isEmpty() {
        return parent.isEmpty();
    }

    @Override
    public void setClean() {
        parent.setClean();
    }

    @Override
    public <T> void register(DataParameter<T> key, @Nonnull T value) {
        parent.register(key, value);
    }

    @Nonnull
    @Override
    public <T> DataEntry<T> getEntry(DataParameter<T> key) {
        return parent.getEntry(key);
    }
}
