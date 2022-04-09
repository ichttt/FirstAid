/*
 * FirstAid
 * Copyright (C) 2017-2022
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageApplyHealingItem {
    private final EnumPlayerPart part;
    private final InteractionHand hand;

    public MessageApplyHealingItem(FriendlyByteBuf buffer) {
        this.part = EnumPlayerPart.VALUES[buffer.readByte()];
        this.hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public MessageApplyHealingItem(EnumPlayerPart part, InteractionHand hand) {
        this.part = part;
        this.hand = hand;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(part.ordinal());
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
    }

    public static class Handler {

        public static void onMessage(final MessageApplyHealingItem message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ServerPlayer player = CommonUtils.checkServer(ctx);
            ctx.enqueueWork(() -> {
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
                ItemStack stack = player.getItemInHand(message.hand);
                Item item = stack.getItem();
                AbstractPartHealer healer = FirstAidRegistryImpl.INSTANCE.getPartHealer(stack);
                if (healer == null) {
                    FirstAid.LOGGER.warn("Player {} has invalid item in hand {} while it should be an healing item", player.getName(), item.getRegistryName());
                    player.sendMessage(new TextComponent("Unable to apply healing item!"), Util.NIL_UUID);
                    return;
                }
                stack.shrink(1);
                AbstractDamageablePart damageablePart = damageModel.getFromEnum(message.part);
                damageablePart.activeHealer = healer;
            });
        }
    }
}
