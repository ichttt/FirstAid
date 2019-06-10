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

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.network.MessageAddHealth;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HealthDistribution {
    private static final List<EnumPlayerPart> parts;
    static {
        EnumPlayerPart[] partArray = EnumPlayerPart.VALUES;
        parts = new ArrayList<>(partArray.length);
        parts.addAll(Arrays.asList(partArray));
    }

    public static void manageHealth(float health, AbstractPlayerDamageModel damageModel, PlayerEntity player, boolean sendChanges, boolean distribute) {
        if (sendChanges && player.world.isRemote) {
            FirstAid.LOGGER.catching(new RuntimeException("Someone set flag sendChanges on the client, this is not supported!"));
            sendChanges = false;
        } else if (sendChanges && !(player instanceof ServerPlayerEntity)) { //EntityOtherPlayerMP? log something?
            sendChanges = false;
        }

        float toHeal = distribute ? health / 8F : health;
        Collections.shuffle(parts, player.world.rand);
        List<AbstractDamageablePart> damageableParts = new ArrayList<>(parts.size());

        for (EnumPlayerPart part : parts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }

        if (distribute)
            damageableParts.sort(Comparator.comparingDouble(value -> value.getMaxHealth() - value.currentHealth));
        float[] healingDone = new float[8];

        for (int i = 0; i < 8; i++) {
            AbstractDamageablePart part = damageableParts.get(i);
            float diff = toHeal - part.heal(toHeal, player, !player.world.isRemote);
            //prevent inaccuracy
            diff = Math.round(diff * 10000.0F) / 10000.0F;
            healingDone[part.part.id - 1] = diff;

            health -= diff;
            if (distribute) {
                if (i < 7)
                    toHeal = health / (7F - i);
            } else {
                toHeal -= diff;
                if (toHeal <= 0)
                    break;
            }
        }

        if (sendChanges)
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageAddHealth(healingDone));
    }

    public static void distributeHealth(float health, PlayerEntity player, boolean sendChanges) {
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        manageHealth(health, damageModel, player, sendChanges, true);
    }

    public static void addRandomHealth(float health, PlayerEntity player, boolean sendChanges) {
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        manageHealth(health, damageModel, player, sendChanges, false);
    }
}
