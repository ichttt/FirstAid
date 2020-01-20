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
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

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

    public static void manageHealth(float health, EntityDamageModel damageModel, EntityLivingBase entity, boolean sendChanges, boolean distribute) {
        if (sendChanges && entity.world.isRemote) {
            FirstAid.LOGGER.catching(new RuntimeException("Someone set flag sendChanges on the client, this is not supported!"));
            sendChanges = false;
        } else if (sendChanges && !(entity instanceof EntityPlayerMP)) {
            sendChanges = false;
        }

        float toHeal = distribute ? health / 8F : health;
        Collections.shuffle(parts, entity.world.rand);

        List<DamageablePart> damageableParts = new ArrayList<>(damageModel.getParts());

        if (distribute)
            damageableParts.sort(Comparator.comparingDouble(value -> value.getMaxHealth() - value.getCurrentHealth()));

        for (int i = 0; i < damageableParts.size(); i++) {
            DamageablePart part = damageableParts.get(i);
            float diff = toHeal - part.heal(toHeal, entity, !entity.world.isRemote);
            //prevent inaccuracy
            diff = Math.round(diff * 10000.0F) / 10000.0F;

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
            FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(damageModel), (EntityPlayerMP) entity);
    }

    public static void distributeHealth(float health, EntityLivingBase player, boolean sendChanges) {
        EntityDamageModel damageModel = player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
        manageHealth(health, damageModel, player, sendChanges, true);
    }

    public static void addRandomHealth(float health, EntityLivingBase player, boolean sendChanges) {
        EntityDamageModel damageModel = player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
        manageHealth(health, damageModel, player, sendChanges, false);
    }
}
