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

package ichttt.mods.firstaid.common.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.DataManagerWrapper;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CommonUtils {
    @Nonnull
    public static final EquipmentSlotType[] ARMOR_SLOTS;
    @Nonnull
    public static final ImmutableMap<EquipmentSlotType, List<EnumPlayerPart>> slotToParts;

    static {
        ARMOR_SLOTS = new EquipmentSlotType[4];
        ARMOR_SLOTS[3] = EquipmentSlotType.HEAD;
        ARMOR_SLOTS[2] = EquipmentSlotType.CHEST;
        ARMOR_SLOTS[1] = EquipmentSlotType.LEGS;
        ARMOR_SLOTS[0] = EquipmentSlotType.FEET;
        slotToParts = ImmutableMap.<EquipmentSlotType, List<EnumPlayerPart>>builder().
        put(EquipmentSlotType.HEAD, Collections.singletonList(EnumPlayerPart.HEAD)).
        put(EquipmentSlotType.CHEST, Arrays.asList(EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM, EnumPlayerPart.BODY)).
        put(EquipmentSlotType.LEGS, Arrays.asList(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG)).
        put(EquipmentSlotType.FEET, Arrays.asList(EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT)).build();
    }

    public static void killPlayer(@Nonnull PlayerEntity player, @Nullable DamageSource source) {
        if (source != null && FirstAidConfig.SERVER.allowOtherHealingItems.get() && player.checkTotemDeathProtection(source))
            return;

//        IRevival revival = getRevivalIfPossible(player);
//        if (revival != null)
//            revival.startBleeding(player, source);
//        else
            ((DataManagerWrapper) player.dataManager).set_impl(PlayerEntity.HEALTH, 0F);
    }

//    /**
//     * Gets the cap, or null if not applicable
//     * @param player The player to check
//     * @return The cap or null if the player cannot be revived
//     */
//    @Nullable
//    public static IRevival getRevivalIfPossible(@Nullable EntityPlayer player) {
//        if (player == null || CapaRevive.reviveCapa == null) TODO PlayerRevival Comapt
//            return null;
//        MinecraftServer server = player.getServer();
//        if (server == null)
//            return null;
//        IRevival revival = player.getCapability(CapaRevive.reviveCapa, null);
//        if (revival != null && server.getPlayerList().getCurrentPlayerCount() > 1)
//            return revival;
//        else
//            return null;
//    }

    public static boolean isValidArmorSlot(EquipmentSlotType slot) {
        return slot.getSlotType() == EquipmentSlotType.Type.ARMOR;
    }

    public static boolean isSurvivalOrAdventure(PlayerEntity player) {
        return !player.isSpectator() && !player.isCreative();
    }

    public static String getActiveModidSafe() {
        ModContainer activeModContainer = ModLoadingContext.get().getActiveContainer();
        return activeModContainer == null ? "UNKNOWN-NULL" : activeModContainer.getModId();
    }

    public static void healPlayerByPercentage(double percentage, AbstractPlayerDamageModel damageModel, PlayerEntity player) {
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

    @Nonnull
    public static AbstractPlayerDamageModel getDamageModel(PlayerEntity player) {
        return player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null).orElseThrow(() -> new IllegalArgumentException("Player " + player.getName() + " is missing a damage model!"));
    }

    public static boolean hasDamageModel(Entity entity) {
        return entity instanceof PlayerEntity && !(entity instanceof FakePlayer);
    }

    @Nonnull
    public static ServerPlayerEntity checkServer(NetworkEvent.Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_SERVER)
            throw new IllegalArgumentException("Wrong side for server packet handler " + context.getDirection());
        context.setPacketHandled(true);
        return Objects.requireNonNull(context.getSender());
    }

    public static void checkClient(NetworkEvent.Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            throw new IllegalArgumentException("Wrong side for client packet handler: " + context.getDirection());
        context.setPacketHandled(true);
    }
}
