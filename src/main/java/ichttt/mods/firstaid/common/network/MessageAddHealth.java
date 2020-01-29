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

import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class MessageAddHealth implements IMessage {
    private float[] table;

    public MessageAddHealth() {}

    public MessageAddHealth(float[] table) {
        this.table = table;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.table = new float[8];
        for (int i = 0; i < 8; i++) {
            this.table[i] = buf.readFloat();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (float f : table)
            buf.writeFloat(f);
    }

    public static class Handler implements IMessageHandler<MessageAddHealth, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MessageAddHealth message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
                AbstractPlayerDamageModel damageModel = Objects.requireNonNull(playerSP.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
                for (int i = 0; i < message.table.length; i++) {
                    float f = message.table[i];
                    //EnumPlayerPart is 1-indexed
                    EnumPlayerPart part = EnumPlayerPart.fromID(i + 1);
                    damageModel.getFromEnum(part).heal(f, playerSP, false);
                }
            });
            return null;
        }
    }
}
