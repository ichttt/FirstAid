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

package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageApplyAbsorption {
    private final float amount;

    public MessageApplyAbsorption(PacketBuffer buffer) {
        amount = buffer.readFloat();
    }

    public MessageApplyAbsorption(float amount) {
        this.amount = amount;
    }

    public void encode(PacketBuffer buf) {
        buf.writeFloat(amount);
    }

    public static class Handler {

        public static void onMessage(MessageApplyAbsorption message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            CommonUtils.checkClient(ctx);
            ctx.enqueueWork(() -> CommonUtils.getDamageModel(Minecraft.getInstance().player).setAbsorption(message.amount));
        }
    }
}
