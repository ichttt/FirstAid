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

package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
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
    private final List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList;
    private final boolean shuffle;

    public StandardDamageDistribution(List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList, boolean shuffle) {
        this.partList = partList;
        for (Pair<EntityEquipmentSlot, EnumPlayerPart[]> pair : partList) {
            for (EnumPlayerPart part : pair.getRight()) {
                if (part.slot != pair.getLeft())
                    throw new RuntimeException(part + " is not a member of " + pair.getLeft());
            }
        }
        this.shuffle = shuffle;
    }

    @Override
    @Nonnull
    protected List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList() {
        if (this.shuffle) Collections.shuffle(this.partList);
        return this.partList;
    }

    @Override
    public float distributeDamage(float damage, @Nonnull EntityPlayer player, @Nonnull DamageSource source, boolean addStat) {
        float rest = super.distributeDamage(damage, player, source, addStat);
        if (rest > 0) {
            EnumPlayerPart[] parts = partList.get(partList.size() - 1).getRight();
            Optional<EnumPlayerPart> playerPart = Arrays.stream(parts).filter(enumPlayerPart -> !enumPlayerPart.getNeighbours().isEmpty()).findAny();
            if (playerPart.isPresent()) {
                List<EnumPlayerPart> neighbours = playerPart.get().getNeighbours();
                neighbours = neighbours.stream().filter(part -> partList.stream().noneMatch(pair -> Arrays.stream(pair.getRight()).anyMatch(p2 -> p2 == part))).collect(Collectors.toList());
                for (EnumPlayerPart part : neighbours)
                    rest = new PreferredDamageDistribution(part).distributeDamage(rest, player, source, addStat);
            }
        }
        return rest;
    }
}
