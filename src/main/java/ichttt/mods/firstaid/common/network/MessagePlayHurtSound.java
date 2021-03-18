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

import ichttt.mods.firstaid.client.DebuffTimedSound;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Supplier;

public class MessagePlayHurtSound {
    private final SoundEvent sound;
    private final int duration;

    public MessagePlayHurtSound(PacketBuffer buffer) {
        this(ForgeRegistries.SOUND_EVENTS.getValue(buffer.readResourceLocation()), buffer.readInt());
    }

    public MessagePlayHurtSound(SoundEvent sound, int duration) {
        this.sound = sound;
        this.duration = duration;
    }

    public void encode(PacketBuffer buf) {
        buf.writeResourceLocation(Objects.requireNonNull(sound.getRegistryName()));
        buf.writeInt(duration);
    }

    public static class Handler {

        public static void onMessage(MessagePlayHurtSound message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            CommonUtils.checkClient(ctx);
            ctx.enqueueWork(() -> DebuffTimedSound.playHurtSound(message.sound, message.duration));
        }
    }
}
