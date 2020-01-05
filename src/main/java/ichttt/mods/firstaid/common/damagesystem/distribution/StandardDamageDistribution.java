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

import ichttt.mods.firstaid.api.enums.EnumBodyPart;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StandardDamageDistribution extends DamageDistribution {
    private final List<Pair<EntityEquipmentSlot, EnumBodyPart[]>> partList;
    private final boolean shuffle;

    public StandardDamageDistribution(List<Pair<EntityEquipmentSlot, EnumBodyPart[]>> partList, boolean shuffle) {
        this.partList = partList;
        for (Pair<EntityEquipmentSlot, EnumBodyPart[]> pair : partList) {
            for (EnumBodyPart part : pair.getRight()) {
                if (part.slot != pair.getLeft())
                    throw new RuntimeException(part + " is not a member of " + pair.getLeft());
            }
        }
        this.shuffle = shuffle;
    }

    @Override
    @Nonnull
    protected List<Pair<EntityEquipmentSlot, EnumBodyPart[]>> getPartList() {
        if (this.shuffle) Collections.shuffle(this.partList);
        return this.partList;
    }

    @Override
    public float distributeDamage(float damage, @Nonnull EntityLivingBase entity, @Nonnull DamageSource source, boolean addStat) {
        float rest = super.distributeDamage(damage, entity, source, addStat);
        if (rest > 0) {
            EnumBodyPart[] parts = partList.get(partList.size() - 1).getRight();
            Optional<EnumBodyPart> playerPart = Arrays.stream(parts).filter(enumPlayerPart -> !enumPlayerPart.getNeighbours().isEmpty()).findAny();
            if (playerPart.isPresent()) {
                List<EnumBodyPart> neighbours = playerPart.get().getNeighbours();
                neighbours = neighbours.stream().filter(part -> partList.stream().noneMatch(pair -> Arrays.stream(pair.getRight()).anyMatch(p2 -> p2 == part))).collect(Collectors.toList());
                for (EnumBodyPart part : neighbours)
                    rest = new PreferredDamageDistribution(part).distributeDamage(rest, entity, source, addStat);
            }
        }
        return rest;
    }
}
