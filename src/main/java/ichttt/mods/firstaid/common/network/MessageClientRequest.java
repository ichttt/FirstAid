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
import ichttt.mods.firstaid.common.CapProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class MessageClientRequest implements IMessage {
    private Type type;

    public MessageClientRequest() {}

    public MessageClientRequest(Type type) {
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = Type.TYPES[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(type.ordinal());
    }

    public enum Type {
        TUTORIAL_COMPLETE, REQUEST_REFRESH;

        private static final Type[] TYPES = values();
    }

    public static class Handler implements IMessageHandler<MessageClientRequest, IMessage> {

        @Override
        public IMessage onMessage(MessageClientRequest message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServer().addScheduledTask(() -> {
                if (message.type == Type.TUTORIAL_COMPLETE) {
                    CapProvider.tutorialDone.add(player.getName());
                    Objects.requireNonNull(player.getServer()).addScheduledTask(() -> Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)).hasTutorial = true);
                } else if (message.type == Type.REQUEST_REFRESH) {
                    FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)), true), player);
                }
            });
            return null;
        }
    }
}
