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

import ichttt.mods.firstaid.client.DebuffTimedSound;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessagePlayHurtSound implements IMessage {
    private SoundEvent sound;
    private int duration;

    public MessagePlayHurtSound () {}

    public MessagePlayHurtSound(SoundEvent sound, int duration) {
        this.sound = sound;
        this.duration = duration;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        sound = ByteBufUtils.readRegistryEntry(buf, ForgeRegistries.SOUND_EVENTS);
        duration = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeRegistryEntry(buf, sound);
        buf.writeInt(duration);
    }

    public static class Handler implements IMessageHandler<MessagePlayHurtSound, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MessagePlayHurtSound message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> DebuffTimedSound.playHurtSound(message.sound, message.duration));
            return null;
        }
    }
}
