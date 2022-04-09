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
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class MessageClientRequest {
    private final Type type;

    public MessageClientRequest(FriendlyByteBuf buffer) {
        this.type = Type.TYPES[buffer.readByte()];
    }

    public MessageClientRequest(Type type) {
        this.type = type;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(type.ordinal());
    }

    public enum Type {
        TUTORIAL_COMPLETE, REQUEST_REFRESH;

        private static final Type[] TYPES = values();
    }

    public static class Handler {

        public static void onMessage(MessageClientRequest message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ServerPlayer player = CommonUtils.checkServer(ctx);
            if (message.type == Type.TUTORIAL_COMPLETE) {
                CapProvider.tutorialDone.add(player.getName().getString());
                ctx.enqueueWork(() -> CommonUtils.getDamageModel(player).hasTutorial = true);
            } else if (message.type == Type.REQUEST_REFRESH) {
                    FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> player), new MessageSyncDamageModel(CommonUtils.getDamageModel(player), true));
            }
        }
    }
}
