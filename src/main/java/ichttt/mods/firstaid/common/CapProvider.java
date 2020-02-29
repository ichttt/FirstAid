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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class CapProvider implements ICapabilitySerializable<CompoundNBT> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(FirstAid.MODID, "cap_adv_dmg_mdl");
    public static final Set<String> tutorialDone = new HashSet<>();
    private final AbstractPlayerDamageModel damageModel;
    private final LazyOptional<AbstractPlayerDamageModel> optional;

    public CapProvider(AbstractPlayerDamageModel damageModel) {
        this.damageModel = damageModel;
        this.optional = LazyOptional.of(() -> damageModel);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityExtendedHealthSystem.INSTANCE)
            return optional.cast();
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return damageModel.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        damageModel.deserializeNBT(nbt);
    }
}
