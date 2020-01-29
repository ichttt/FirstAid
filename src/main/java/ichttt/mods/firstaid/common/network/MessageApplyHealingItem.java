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

package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class MessageApplyHealingItem implements IMessage {
    private EnumPlayerPart part;
    private EnumHand hand;

    public MessageApplyHealingItem() {}

    public MessageApplyHealingItem(EnumPlayerPart part, EnumHand hand) {
        this.part = part;
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        part = EnumPlayerPart.fromID(buf.readByte());
        hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(part.id);
        buf.writeBoolean(hand == EnumHand.MAIN_HAND);
    }

    public static class Handler implements IMessageHandler<MessageApplyHealingItem, IMessage> {

        @Override
        public IMessage onMessage(final MessageApplyHealingItem message, final MessageContext ctx) {
            //noinspection ConstantConditions
            ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                AbstractPlayerDamageModel damageModel = Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
                ItemStack stack = player.getHeldItem(message.hand);
                Item item = stack.getItem();
                AbstractPartHealer healer = FirstAidRegistryImpl.INSTANCE.getPartHealer(stack);
                if (healer == null) {
                    FirstAid.LOGGER.warn("Player {} has invalid item in hand {} while it should be an healing item", player.getName(), item.getRegistryName());
                    player.sendMessage(new TextComponentString("Unable to apply healing item!"));
                    return;
                }
                stack.shrink(1);
                AbstractDamageablePart damageablePart = damageModel.getFromEnum(message.part);
                damageablePart.activeHealer = healer;
            });
            return null;
        }
    }
}
