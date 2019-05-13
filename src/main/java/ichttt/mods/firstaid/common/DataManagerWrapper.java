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
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.network.MessageApplyAbsorption;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This is a hack to intervene all calls to absorption. It's not optimal but it's the best I could come up with without a coremod
 * This should be compatible with other mods which do so as I respect the parent in any other case.
 */
public class DataManagerWrapper extends EntityDataManager {
    private final EntityPlayer player;
    private final EntityDataManager parent;

    public DataManagerWrapper(EntityPlayer player, EntityDataManager parent) {
        super(player);
        this.player = player;
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public <T> T get(@Nonnull DataParameter<T> key) {
        if (key == EntityPlayer.ABSORPTION)
            parent.set(key, (T) Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)).getAbsorption());
        return parent.get(key);
    }

    public <T> void set_impl(@Nonnull DataParameter<T> key, @Nonnull T value) {
        parent.set(key, value);
    }

    @Override
    public <T> void set(@Nonnull DataParameter<T> key, @Nonnull T value) {
        if (key == EntityPlayer.ABSORPTION) {
            float floatValue = (Float) value;
            if (player instanceof EntityPlayerMP) { //may be EntityOtherPlayerMP as well
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (playerMP.connection != null) //also fired when connecting, ignore(otherwise the net handler would crash)
                    FirstAid.NETWORKING.sendTo(new MessageApplyAbsorption(floatValue), playerMP);
            }
            Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)).setAbsorption(floatValue);
        } else if (key == EntityLivingBase.HEALTH) {
            if (value instanceof Float) {
                Float aFloat = (Float) value;
                if (aFloat > player.getMaxHealth()) {
                    if (player.world.isRemote) //I don't know why only if !world.isRemote... maybe double check this
                        Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)).forEach(damageablePart -> damageablePart.currentHealth = damageablePart.getMaxHealth());
                } else if (FirstAidConfig.watchSetHealth && !aFloat.isInfinite() && !aFloat.isNaN() && aFloat > 0 && !player.world.isRemote && player instanceof EntityPlayerMP && ((EntityPlayerMP) player).connection != null) {
                    //calculate diff
                    Float orig = get(EntityLivingBase.HEALTH);
                    if (orig > 0 && !orig.isNaN() && !orig.isInfinite()) {
                        float healed = aFloat - orig;
                        if (Math.abs(healed) > 0.001) {
                            if (healed < 0) {
                                if (FirstAid.DEBUG) {
                                    CommonUtils.debugLogStacktrace("DAMAGING: " + (-healed));
                                }
                                DamageDistribution.handleDamageTaken(RandomDamageDistribution.NEAREST_KILL, player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null), -healed, player, DamageSource.MAGIC, true, true);
                            } else {
                                if (FirstAid.DEBUG) {
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
    public <T> void register(DataParameter<T> key, @Nonnull T value) {
        parent.register(key, value);
    }

    @Override
    public <T> void setDirty(@Nonnull DataParameter<T> key) {
        parent.setDirty(key);
    }

    @Override
    public boolean isDirty() {
        return parent.isDirty();
    }

    @Nullable
    @Override
    public List<DataEntry<?>> getDirty() {
        return parent.getDirty();
    }

    @Override
    public void writeEntries(PacketBuffer buf) throws IOException {
        parent.writeEntries(buf);
    }

    @Nullable
    @Override
    public List<DataEntry<?>> getAll() {
        return parent.getAll();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setEntryValues(List<DataEntry<?>> entriesIn) {
        parent.setEntryValues(entriesIn);
    }

    @Override
    @SideOnly(Side.CLIENT)
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
    @Nonnull
    public <T> DataEntry<T> getEntry(DataParameter<T> key) {
        return parent.getEntry(key);
    }
}
