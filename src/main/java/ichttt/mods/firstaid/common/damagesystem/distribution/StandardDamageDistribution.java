/*
 * FirstAid
 * Copyright (C) 2017-2021
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StandardDamageDistribution extends DamageDistribution {
    private final List<Pair<EquipmentSlot, EnumPlayerPart[]>> partList;
    private final boolean shuffle;
    private final boolean doNeighbours;
    private final EnumSet<EnumPlayerPart> blockedParts;

    public StandardDamageDistribution(List<Pair<EquipmentSlot, EnumPlayerPart[]>> partList, boolean shuffle, boolean doNeighbours) {
        this.partList = partList;
        for (Pair<EquipmentSlot, EnumPlayerPart[]> pair : partList) {
            for (EnumPlayerPart part : pair.getRight()) {
                if (part.slot != pair.getLeft())
                    throw new RuntimeException(part + " is not a member of " + pair.getLeft());
            }
        }
        this.shuffle = shuffle;
        this.doNeighbours = doNeighbours;
        this.blockedParts = EnumSet.noneOf(EnumPlayerPart.class);
    }

    // Private constructor, no validation required
    // This is done for speed, as these are temp distributions for the redistribution
    private StandardDamageDistribution(List<Pair<EquipmentSlot, EnumPlayerPart[]>> partList, boolean shuffle, boolean doNeighbours, EnumSet<EnumPlayerPart> blockedParts) {
        this.partList = partList;
        this.shuffle = shuffle;
        this.doNeighbours = doNeighbours;
        this.blockedParts = blockedParts;
    }

    @Override
    @Nonnull
    protected List<Pair<EquipmentSlot, EnumPlayerPart[]>> getPartList() {
        if (this.shuffle) Collections.shuffle(this.partList);
        return this.partList;
    }

    @Override
    public float distributeDamage(float damage, @Nonnull Player player, @Nonnull DamageSource source, boolean addStat) {
        float rest = super.distributeDamage(damage, player, source, addStat);
        if (rest > 0 && doNeighbours) {
            EnumSet<EnumPlayerPart> neighboursSet = EnumSet.noneOf(EnumPlayerPart.class);

            // Calculate the set of blocked parts that don't need to be considered for redistribution
            EnumSet<EnumPlayerPart> blockedParts = EnumSet.copyOf(this.blockedParts);
            for (Pair<EquipmentSlot, EnumPlayerPart[]> pair : this.partList) {
                blockedParts.addAll(Arrays.asList(pair.getRight()));
            }

            for (int i = partList.size() - 1; i >= 0; i--) {
                // We still need to distribute some damage. Start with last element of the distribution, and search for possible neighbours
                // Then, if there is still some damage that needs redistribution, go to the next layer
                EnumPlayerPart[] parts = partList.get(i).getRight();
                for (EnumPlayerPart part : parts) {
                    neighboursSet.addAll(part.getNeighbours());
                }

                neighboursSet.removeIf(blockedParts::contains);
                if (!neighboursSet.isEmpty()) {
                    // Found allowed neighbours for this distribution layer. Try to redistribute
                    List<EnumPlayerPart> neighbours = new ArrayList<>(neighboursSet);
                    Collections.shuffle(neighbours);
                    Map<EquipmentSlot, List<EnumPlayerPart>> neighbourMapping = new LinkedHashMap<>();
                    for (EnumPlayerPart neighbour : neighbours) {
                        neighbourMapping.computeIfAbsent(neighbour.slot, type -> new ArrayList<>(3)).add(neighbour);
                    }

                    List<Pair<EquipmentSlot, EnumPlayerPart[]>> neighbourDistributions = new ArrayList<>();
                    for (Map.Entry<EquipmentSlot, List<EnumPlayerPart>> entry : neighbourMapping.entrySet()) {
                        neighbourDistributions.add(Pair.of(entry.getKey(), entry.getValue().toArray(new EnumPlayerPart[0])));
                    }


                    // shuffle can be false, we already shuffle above. Always do neighbours to have a predictable order
                    StandardDamageDistribution remainingDistribution = new StandardDamageDistribution(neighbourDistributions, false, true, blockedParts);
                    rest = remainingDistribution.distributeDamage(rest, player, source, addStat);
                    if (rest <= 0F) break; //Check if we actually need to do next layer or if it is fine with this iteration

                    // Still got some damage left. Add the now drained parts to the blocked list. Take the block list from the temp distribution
                    // This is based on the old block list, so we can just replace instead of add it
                    blockedParts = remainingDistribution.blockedParts;
                    neighboursSet.clear();
                }
            }
        }
        return rest;
    }
}
