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

package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.common.compat.playerrevive.PRCompatManager;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.network.MessageApplyAbsorption;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This is a hack to intervene all calls to absorption. It's not optimal but it's the best I could come up with without a coremod
 * This should be compatible with other mods which do so as I respect the parent in any other case.
 */
public class SynchedEntityDataWrapper extends SynchedEntityData {
    private final Player player;
    private final SynchedEntityData parent;
    private boolean track = true;

    public SynchedEntityDataWrapper(Player player, SynchedEntityData parent) {
        super(player);
        this.player = player;
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public <T> T get(@Nonnull EntityDataAccessor<T> key) {
        if (key == Player.DATA_PLAYER_ABSORPTION_ID && player.isAlive())
            parent.set(key, (T) CommonUtils.getDamageModel(player).getAbsorption());
        return parent.get(key);
    }

    public <T> void set_impl(@Nonnull EntityDataAccessor<T> key, @Nonnull T value) {
        parent.set(key, value);
    }

    @Override
    public <T> void set(@Nonnull EntityDataAccessor<T> key, @Nonnull T value) {
        if (!track) {
            if (key != LivingEntity.DATA_HEALTH_ID)
                set_impl(key, value);
            return;
        }

        if (key == Player.DATA_PLAYER_ABSORPTION_ID) {
            float floatValue = (Float) value;
            if (player instanceof ServerPlayer) { //may be EntityOtherPlayerMP as well
                ServerPlayer playerMP = (ServerPlayer) player;
                if (playerMP.connection != null) //also fired when connecting, ignore(otherwise the net handler would crash)
                    FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> playerMP), new MessageApplyAbsorption(floatValue));
            }
            CommonUtils.getDamageModel(player).setAbsorption(floatValue);
        } else if (key == LivingEntity.DATA_HEALTH_ID) {
            // AVERT YOUR EYES - this code is barely readable and very hacky
            if (value instanceof Float && !player.level.isClientSide) {
                float aFloat = (Float) value;
                if (aFloat > player.getMaxHealth()) {
                    CommonUtils.getDamageModel(player).forEach(damageablePart -> damageablePart.currentHealth = damageablePart.getMaxHealth());
                } else if (PRCompatManager.getHandler().isBleeding(player, true)) {
                    if (FirstAidConfig.GENERAL.debug.get())
                        CommonUtils.debugLogStacktrace("Completely ignoring setHealth!");
                    return;
                } else if (FirstAidConfig.watchSetHealth && !Float.isInfinite(aFloat) && !Float.isNaN(aFloat) && aFloat > 0 && player instanceof ServerPlayer && ((ServerPlayer) player).connection != null) {
                    //calculate diff
                    float orig = get(LivingEntity.DATA_HEALTH_ID);
                    if (orig > 0 && !Float.isNaN(orig) && !Float.isInfinite(orig)) {
                        if (FirstAidConfig.SERVER.scaleMaxHealth.get())
                            orig = Math.min(orig, (float) this.player.getAttribute(Attributes.MAX_HEALTH).getValue());
                        float healed = aFloat - orig;
                        if (Math.abs(healed) > 0.001) {
                            if (healed < 0) {
                                if (FirstAidConfig.GENERAL.debug.get()) {
                                    CommonUtils.debugLogStacktrace("DAMAGING: " + (-healed));
                                }
                                DamageDistribution.handleDamageTaken(RandomDamageDistribution.getDefault(), CommonUtils.getDamageModel(player), -healed, player, DamageSource.MAGIC, true, true);
                            } else {
                                if (FirstAidConfig.GENERAL.debug.get()) {
                                    CommonUtils.debugLogStacktrace("HEALING: " + healed);
                                }
                                HealthDistribution.addRandomHealth(healed, player, true);
                            }
                        }
                        return;
                    }
                }
            }
        }
        set_impl(key, value);
    }


    public void toggleTracking(boolean status) {
        if (FirstAidConfig.GENERAL.debug.get())
            CommonUtils.debugLogStacktrace("Tracking status change from " + track + " to " + status);
        track = status;
    }

    // ----------WRAPPER BELOW----------

    @Override
    public boolean isDirty() {
        return parent.isDirty();
    }

    @Override
    @Nullable
    public List<DataItem<?>> packDirty() {
        return parent.packDirty();
    }

    @Override
    @Nullable
    public List<DataItem<?>> getAll() {
        return parent.getAll();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void assignValues(List<DataItem<?>> entriesIn) {
        parent.assignValues(entriesIn);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T> void assignValue(DataItem<T> target, DataItem<?> source) {
        parent.assignValue(target, source);
    }

    @Override
    public boolean isEmpty() {
        return parent.isEmpty();
    }

    @Override
    public void clearDirty() {
        parent.clearDirty();
    }

    @Override
    public <T> void define(EntityDataAccessor<T> key, @Nonnull T value) {
        parent.define(key, value);
    }

    @Nonnull
    @Override
    public <T> DataItem<T> getItem(EntityDataAccessor<T> key) {
        return parent.getItem(key);
    }
}
