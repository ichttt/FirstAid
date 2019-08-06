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

import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageReceiveDamage {

    private final EnumPlayerPart part;
    private final float damageAmount;
    private final float minHealth;

    public MessageReceiveDamage(PacketBuffer buffer) {
        this(EnumPlayerPart.VALUES[buffer.readByte()], buffer.readFloat(), buffer.readFloat());
    }

    public MessageReceiveDamage(EnumPlayerPart part, float damageAmount, float minHealth) {
        this.part = part;
        this.damageAmount = damageAmount;
        this.minHealth = minHealth;
    }

    public void encode(PacketBuffer buf) {
        buf.writeByte(part.ordinal());
        buf.writeFloat(damageAmount);
        buf.writeFloat(minHealth);
    }

    public static class Handler {

        public static void onMessage(MessageReceiveDamage message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            CommonUtils.checkClient(ctx);
            ctx.enqueueWork(() -> {
               AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(Minecraft.getInstance().player);
               AbstractDamageablePart part = damageModel.getFromEnum(message.part);
               if (message.damageAmount > 0F)
                   part.damage(message.damageAmount, null, false, message.minHealth);
               else if (message.damageAmount < 0F)
                   part.heal(-message.damageAmount, null, false);
            });
        }
    }
}
