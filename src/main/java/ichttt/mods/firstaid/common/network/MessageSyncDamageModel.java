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

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSyncDamageModel {
    private final CompoundNBT playerDamageModel;
    private final boolean scaleMaxHealth;

    public MessageSyncDamageModel(PacketBuffer buffer) {
        this.playerDamageModel = buffer.readCompoundTag();
        this.scaleMaxHealth = buffer.readBoolean();
    }

    public MessageSyncDamageModel(AbstractPlayerDamageModel damageModel, boolean scaleMaxHealth) {
        this.playerDamageModel = damageModel.serializeNBT();
        this.scaleMaxHealth = scaleMaxHealth;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeCompoundTag(this.playerDamageModel);
        buffer.writeBoolean(scaleMaxHealth);
    }

    public static final class Handler {

        public static void onMessage(MessageSyncDamageModel message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            CommonUtils.checkClient(ctx);
            ctx.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(mc.player);
                if (message.scaleMaxHealth)
                    damageModel.runScaleLogic(mc.player);
                damageModel.deserializeNBT(message.playerDamageModel);
            });
        }
    }
}
