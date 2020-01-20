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

package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class PreferredDamageDistribution extends DamageDistribution {
    private final EntityEquipmentSlot slot;

    public PreferredDamageDistribution(EnumPlayerPart preferred) {
        this.slot = preferred.slot;
    }

    public PreferredDamageDistribution(EntityEquipmentSlot slot) {
        this.slot = slot;
    }

    @Nonnull
    @Override
    protected List<Pair<EntityEquipmentSlot, List<DamageablePart>>> getPartList(EntityDamageModel damageModel, EntityLivingBase entity) {
        return Collections.singletonList(Pair.of(slot, damageModel.getParts(this.slot)));
    }
}
