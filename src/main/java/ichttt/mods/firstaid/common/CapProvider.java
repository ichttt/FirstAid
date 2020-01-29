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

package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class CapProvider implements ICapabilitySerializable<NBTTagCompound> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(FirstAid.MODID, "capExtendedHealthSystem");
    public static final Set<String> tutorialDone = new HashSet<>();
    private final AbstractPlayerDamageModel damageModel;

    public CapProvider(AbstractPlayerDamageModel damageModel) {
        this.damageModel = damageModel;
    }
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityExtendedHealthSystem.INSTANCE;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityExtendedHealthSystem.INSTANCE)
            return CapabilityExtendedHealthSystem.INSTANCE.cast(damageModel);
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return damageModel.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        damageModel.deserializeNBT(nbt);
    }
}
