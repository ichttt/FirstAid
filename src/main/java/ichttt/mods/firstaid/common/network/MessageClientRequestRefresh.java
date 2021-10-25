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
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageClientRequestRefresh {
    private final UUID playerUUID;

    public MessageClientRequestRefresh(PacketBuffer buffer) {
        this.playerUUID = buffer.readUUID();
    }

    public MessageClientRequestRefresh(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void encode(PacketBuffer buf) {
        buf.writeUUID(playerUUID);
    }

    public enum Type {
        TUTORIAL_COMPLETE, REQUEST_REFRESH;

        private static final Type[] TYPES = values();
    }

    public static class Handler {

        public static void onMessage(MessageClientRequestRefresh message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ServerPlayerEntity player = CommonUtils.checkServer(ctx);
            ServerPlayerEntity target = player.server.getPlayerList().getPlayer(message.playerUUID);
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> player), new MessageSyncDamageModel(CommonUtils.getDamageModel(target), true, target.getUUID()));
        }
    }
}
