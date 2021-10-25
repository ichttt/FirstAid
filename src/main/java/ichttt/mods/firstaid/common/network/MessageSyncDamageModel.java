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

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageSyncDamageModel {
    private final CompoundNBT playerDamageModel;
    private final boolean scaleMaxHealth;
    private final UUID playerUUID;

    public MessageSyncDamageModel(PacketBuffer buffer) {
        this.playerDamageModel = buffer.readNbt();
        this.scaleMaxHealth = buffer.readBoolean();
        this.playerUUID = buffer.readUUID();
    }

    public MessageSyncDamageModel(AbstractPlayerDamageModel damageModel, boolean scaleMaxHealth, UUID playerUUID) {
        this.playerDamageModel = damageModel.serializeNBT();
        this.scaleMaxHealth = scaleMaxHealth;
        this.playerUUID = playerUUID;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeNbt(this.playerDamageModel);
        buffer.writeBoolean(scaleMaxHealth);
        buffer.writeUUID(playerUUID);
    }

    public static final class Handler {

        public static void onMessage(MessageSyncDamageModel message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            CommonUtils.checkClient(ctx);
            ctx.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                PlayerEntity player = mc.level.getPlayerByUUID(message.playerUUID);
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
                if (message.scaleMaxHealth)
                    damageModel.runScaleLogic(player);
                damageModel.deserializeNBT(message.playerDamageModel);
            });
        }
    }
}
