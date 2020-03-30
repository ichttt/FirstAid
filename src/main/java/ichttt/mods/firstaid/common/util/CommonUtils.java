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

package ichttt.mods.firstaid.common.util;

import com.creativemd.playerrevive.api.IRevival;
import com.creativemd.playerrevive.api.capability.CapaRevive;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.DataManagerWrapper;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CommonUtils {
    @Nonnull
    public static final EntityEquipmentSlot[] ARMOR_SLOTS;
    @Nonnull
    public static final ImmutableMap<EntityEquipmentSlot, List<EnumPlayerPart>> slotToParts;

    static {
        ARMOR_SLOTS = new EntityEquipmentSlot[4];
        ARMOR_SLOTS[3] = EntityEquipmentSlot.HEAD;
        ARMOR_SLOTS[2] = EntityEquipmentSlot.CHEST;
        ARMOR_SLOTS[1] = EntityEquipmentSlot.LEGS;
        ARMOR_SLOTS[0] = EntityEquipmentSlot.FEET;
        slotToParts = ImmutableMap.<EntityEquipmentSlot, List<EnumPlayerPart>>builder().
        put(EntityEquipmentSlot.HEAD, Collections.singletonList(EnumPlayerPart.HEAD)).
        put(EntityEquipmentSlot.CHEST, Arrays.asList(EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM, EnumPlayerPart.BODY)).
        put(EntityEquipmentSlot.LEGS, Arrays.asList(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG)).
        put(EntityEquipmentSlot.FEET, Arrays.asList(EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT)).build();
    }

    public static void killPlayer(@Nonnull AbstractPlayerDamageModel damageModel, @Nonnull EntityPlayer player, @Nullable DamageSource source) {
        if (player.world.isRemote) {
            try {
                throw new RuntimeException("Tried to kill the player on the client!");
            } catch (RuntimeException e) {
                FirstAid.LOGGER.warn("Tried to kill the player on the client! This should only happen on the server! Ignoring...", e);
            }
        }
        if (source != null && FirstAidConfig.externalHealing.allowOtherHealingItems) {
            DataManagerWrapper wrapper = (DataManagerWrapper) player.dataManager;
            boolean protection;
            wrapper.toggleTracking(false);
            try {
                //totem protected the player - make sure he actually isn't dead
                protection = player.checkTotemDeathProtection(source);
            } finally {
                wrapper.toggleTracking(true);
            }
            if (protection) {
                for (AbstractDamageablePart part : damageModel) {
                    if (part.canCauseDeath)
                        part.currentHealth = Math.max(part.currentHealth, 1F);
                }
                if (player instanceof EntityPlayerMP)
                    FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(damageModel, false), (EntityPlayerMP) player);
                return;
            }
        }

        IRevival revival = getRevivalIfPossible(player);
        if (revival != null)
            revival.startBleeding(player, source);
        else
            ((DataManagerWrapper) player.dataManager).set_impl(EntityPlayer.HEALTH, 0F);
    }

    /**
     * Gets the cap, or null if not applicable
     * @param player The player to check
     * @return The cap or null if the player cannot be revived
     */
    @Nullable
    public static IRevival getRevivalIfPossible(@Nullable EntityPlayer player) {
        if (player == null || CapaRevive.reviveCapa == null)
            return null;
        MinecraftServer server = player.getServer();
        if (server == null)
            return null;
        IRevival revival = player.getCapability(CapaRevive.reviveCapa, null);
        if (revival != null && server.getPlayerList().getCurrentPlayerCount() > 1)
            return revival;
        else
            return null;
    }

    public static boolean isValidArmorSlot(EntityEquipmentSlot slot) {
        return slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR;
    }

    public static boolean isSurvivalOrAdventure(EntityPlayer player) {
        return !player.isSpectator() && !player.isCreative();
    }

    @Nonnull
    public static String getActiveModidSafe() {
        ModContainer activeModContainer = Loader.instance().activeModContainer();
        return activeModContainer == null ? "UNKNOWN-NULL" : activeModContainer.getModId();
    }

    public static void healPlayerByPercentage(double percentage, AbstractPlayerDamageModel damageModel, EntityPlayer player) {
        Objects.requireNonNull(damageModel);
        int healValue = Ints.checkedCast(Math.round(damageModel.getCurrentMaxHealth() * percentage));
        HealthDistribution.manageHealth(healValue, damageModel, player, true, false);
    }

    public static void debugLogStacktrace(String name) {
        if (!FirstAidConfig.debug) return;
        try {
            throw new RuntimeException("DEBUG:" + name);
        } catch (RuntimeException e) {
            FirstAid.LOGGER.info("DEBUG: " + name, e);
        }
    }
}
