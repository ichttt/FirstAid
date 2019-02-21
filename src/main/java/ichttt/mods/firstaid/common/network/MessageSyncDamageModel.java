/*
 * FirstAid
 * Copyright (C) 2017-2018
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSyncDamageModel {
    private final NBTTagCompound playerDamageModel;

    public MessageSyncDamageModel(PacketBuffer buffer) {
        this.playerDamageModel = buffer.readCompoundTag();
    }

    public MessageSyncDamageModel(AbstractPlayerDamageModel damageModel) {
        this.playerDamageModel = damageModel.serializeNBT();
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeCompoundTag(this.playerDamageModel);
    }

    public static final class Handler {

        public static void onMessage(MessageSyncDamageModel message, Supplier<NetworkEvent.Context> supplier) {
            Minecraft mc = Minecraft.getInstance();
            mc.addScheduledTask(() -> CommonUtils.getDamageModel(mc.player).deserializeNBT(message.playerDamageModel));
        }
    }
}
