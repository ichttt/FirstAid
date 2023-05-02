/*
 * FirstAid
 * Copyright (C) 2017-2023
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class HealthDistribution {
    private static final List<EnumPlayerPart> parts;
    static {
        EnumPlayerPart[] partArray = EnumPlayerPart.VALUES;
        parts = new ArrayList<>(partArray.length);
        parts.addAll(Arrays.asList(partArray));
    }

    public static void manageHealth(float health, AbstractPlayerDamageModel damageModel, Player player, boolean sendChanges, boolean distribute) {
        if (sendChanges && player.level.isClientSide) {
            FirstAid.LOGGER.warn("The sendChanges flag was set on the client, it can however only work on the server!" ,new RuntimeException("sendChanges flag set on the client, this is not supported!"));
            sendChanges = false;
        } else if (sendChanges && !(player instanceof ServerPlayer)) { //EntityOtherPlayerMP? log something?
            sendChanges = false;
        }

        float toHeal = distribute ? health / 8F : health;
        Collections.shuffle(parts);
        List<AbstractDamageablePart> damageableParts = new ArrayList<>(parts.size());

        for (EnumPlayerPart part : parts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }

        if (distribute)
            damageableParts.sort(Comparator.comparingDouble(value -> value.getMaxHealth() - value.currentHealth));
        float[] healingDone = new float[8];

        for (int i = 0; i < 8; i++) {
            AbstractDamageablePart part = damageableParts.get(i);
            float diff = toHeal - part.heal(toHeal, player, !player.level.isClientSide);
            //prevent inaccuracy
            diff = Math.round(diff * 10000.0F) / 10000.0F;
            healingDone[part.part.ordinal()] = diff;

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

        if (sendChanges) {
            ServerPlayer playerMP = (ServerPlayer) player;
            if (playerMP.connection == null || playerMP.connection.connection == null)
                damageModel.scheduleResync(); //Too early to send changes, keep in mind and do it later
            else
                FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> playerMP), new MessageAddHealth(healingDone));
        }
    }

    public static void distributeHealth(float health, Player player, boolean sendChanges) {
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        manageHealth(health, damageModel, player, sendChanges, true);
    }

    public static void addRandomHealth(float health, Player player, boolean sendChanges) {
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        manageHealth(health, damageModel, player, sendChanges, false);
    }
}
