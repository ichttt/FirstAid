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
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class MessageSyncDamageModel implements IMessage {
    private NBTTagCompound playerDamageModel;
    private boolean scaleMaxHealth;

    public MessageSyncDamageModel() {}

    public MessageSyncDamageModel(AbstractPlayerDamageModel damageModel, boolean scaleMaxHealth) {
        this.playerDamageModel = damageModel.serializeNBT();
        this.scaleMaxHealth = scaleMaxHealth;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerDamageModel = ByteBufUtils.readTag(buf);
        scaleMaxHealth = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.playerDamageModel);
        buf.writeBoolean(scaleMaxHealth);
    }

    public static final class Handler implements IMessageHandler<MessageSyncDamageModel, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MessageSyncDamageModel message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                AbstractPlayerDamageModel damageModel = Objects.requireNonNull(mc.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
                if (message.scaleMaxHealth)
                    damageModel.runScaleLogic(mc.player);
                damageModel.deserializeNBT(message.playerDamageModel);
            });
            return null;
        }
    }
}
