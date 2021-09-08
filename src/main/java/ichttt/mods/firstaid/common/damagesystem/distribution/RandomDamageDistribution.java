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

import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomDamageDistribution extends DamageDistribution {
    public static final RandomDamageDistribution NEAREST_NOKILL = new RandomDamageDistribution(true, true);
    public static final RandomDamageDistribution NEAREST_KILL = new RandomDamageDistribution(true, false);
    public static final RandomDamageDistribution ANY_NOKILL = new RandomDamageDistribution(false, true);
    public static final RandomDamageDistribution ANY_KILL = new RandomDamageDistribution(false, false);

    public static RandomDamageDistribution getDefault() {
        return FirstAidConfig.SERVER.useFriendlyRandomDistribution.get() ? NEAREST_NOKILL : NEAREST_KILL;
    }

    private static final Random RANDOM = new Random();
    private final boolean nearestFirst;
    private final boolean tryNoKill;

    protected RandomDamageDistribution(boolean nearestFirst, boolean tryNoKill) {
        this.nearestFirst = nearestFirst;
        this.tryNoKill = tryNoKill;
    }

    @Override
    protected float minHealth(@Nonnull Player player, @Nonnull AbstractDamageablePart playerPart) {
        if (tryNoKill && playerPart.canCauseDeath)
            return 1F;
        return 0F;
    }

    @Override
    @Nonnull
    protected List<Pair<EquipmentSlot, EnumPlayerPart[]>> getPartList() {
        if (nearestFirst) {
            int startValue = RANDOM.nextInt(4);
            return addAllRandom(startValue, RANDOM.nextBoolean());
        } else {
            List<Pair<EquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();
            List<EquipmentSlot> slots = Arrays.asList(EquipmentSlot.values());
            Collections.shuffle(slots, RANDOM);
            for (EquipmentSlot slot : slots) {
                if (!CommonUtils.isValidArmorSlot(slot))
                    continue;
                List<EnumPlayerPart> parts = CommonUtils.getPartListForSlot(slot);
                Collections.shuffle(parts);
                partList.add(Pair.of(slot, parts.toArray(new EnumPlayerPart[0])));
            }
            return partList;
        }
    }

    public static List<Pair<EquipmentSlot, EnumPlayerPart[]>> addAllRandom(int startValue, boolean up) {
        List<Pair<EquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();
        for (int i = 0; i < CommonUtils.ARMOR_SLOTS.length; i ++) {
            int posInArray = Math.abs(i - (up ? 0 : 3)) + startValue;
            if (posInArray > 3)
                posInArray -= 4;
            EquipmentSlot slot = CommonUtils.ARMOR_SLOTS[posInArray];
            List<EnumPlayerPart> parts = CommonUtils.getPartListForSlot(slot);
            Collections.shuffle(parts);
            partList.add(Pair.of(slot, parts.toArray(new EnumPlayerPart[0])));
        }
        return partList;
    }
}
