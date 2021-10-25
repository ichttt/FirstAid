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

package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.loading.FMLCommonLaunchHandler;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageApplyHealingItem {
    private final EnumPlayerPart part;
    private final Hand hand;
    private final UUID playerUUID;

    public MessageApplyHealingItem(PacketBuffer buffer) {
        this.part = EnumPlayerPart.VALUES[buffer.readByte()];
        this.hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        this.playerUUID = buffer.readUUID();
    }

    public MessageApplyHealingItem(EnumPlayerPart part, Hand hand, UUID playerUUID) {
        this.part = part;
        this.hand = hand;
        this.playerUUID = playerUUID;
    }

    public void encode(PacketBuffer buf) {
        buf.writeByte(part.ordinal());
        buf.writeBoolean(hand == Hand.MAIN_HAND);
        buf.writeUUID(playerUUID);
    }

    public static class Handler {

        public static void onMessage(final MessageApplyHealingItem message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ServerPlayerEntity player = CommonUtils.checkServer(ctx);
            ServerPlayerEntity target = player.server.getPlayerList().getPlayer(message.playerUUID);
            ctx.enqueueWork(() -> {
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(target);
                ItemStack stack = player.getItemInHand(message.hand);
                Item item = stack.getItem();
                AbstractPartHealer healer = FirstAidRegistryImpl.INSTANCE.getPartHealer(stack);
                if (healer == null) {
                    FirstAid.LOGGER.warn("Player {} has invalid item in hand {} while it should be an healing item", player.getName(), item.getRegistryName());
                    player.sendMessage(new StringTextComponent("Unable to apply healing item!"), Util.NIL_UUID);
                    return;
                }
                stack.shrink(1);
                AbstractDamageablePart damageablePart = damageModel.getFromEnum(message.part);
                damageablePart.activeHealer = healer;
                FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> target), new MessageSyncDamageModel(CommonUtils.getDamageModel(target), true, target.getUUID()));
            });
        }
    }
}
