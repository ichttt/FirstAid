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

package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class DebuffTimedSound implements ITickableSound {
    private static final float volumeMultiplier = 1.25F;
    private final float minusPerTick;
    private final int debuffDuration;
    private final ResourceLocation soundLocation;
    private final SoundEvent event;
    private final WeakReference<ClientPlayerEntity> player;
    private Sound sound;
    private float volume = volumeMultiplier;
    private int ticks;
    private final static Map<SoundEvent, DebuffTimedSound> activeSounds = new HashMap<>();

    public static void playHurtSound(SoundEvent event, int duration) {
        if (!FirstAidConfig.enableSoundSystem)
            return;
        SoundHandler soundHandler = Minecraft.getInstance().getSoundHandler();
        DebuffTimedSound matchingSound = activeSounds.get(event);
        if (matchingSound != null) {
            if (!matchingSound.isDonePlaying())
                soundHandler.stop(matchingSound);
            activeSounds.remove(event);
        }
        DebuffTimedSound newSound = new DebuffTimedSound(event, duration);
        soundHandler.play(newSound);
        activeSounds.put(event, newSound);
    }

    public DebuffTimedSound(SoundEvent event, int debuffDuration) {
        this.event = event;
        this.soundLocation = event.getName();
        this.player = new WeakReference<>(Minecraft.getInstance().player);
        this.debuffDuration = Integer.min(15 * 20, debuffDuration);
        this.minusPerTick = (1F / this.debuffDuration) * volumeMultiplier;
    }

    @Override
    public boolean isDonePlaying() {
        ClientPlayerEntity player = this.player.get();
        boolean done = player == null || ticks >= debuffDuration || player.getHealth() <= 0;
        if (done)
            activeSounds.remove(this.event);
        return done;
    }

    @Nonnull
    @Override
    public ResourceLocation getSoundLocation() {
        return soundLocation;
    }

    @Nullable
    @Override
    public SoundEventAccessor createAccessor(@Nonnull SoundHandler handler) {
        SoundEventAccessor soundEventAccessor = handler.getAccessor(this.soundLocation);

        if (soundEventAccessor == null)
        {
            FirstAid.LOGGER.warn("Missing sound for location " + this.soundLocation);
            this.sound = SoundHandler.MISSING_SOUND;
        }
        else
        {
            this.sound = soundEventAccessor.cloneEntry();
        }

        return soundEventAccessor;
    }

    @Nonnull
    @Override
    public Sound getSound() {
        return sound;
    }

    @Nonnull
    @Override
    public SoundCategory getCategory() {
        return SoundCategory.PLAYERS;
    }

    @Override
    public boolean canRepeat() {
        return false;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return 1F;
    }

    @Override
    public float getX() {
        ClientPlayerEntity player = this.player.get();
        if (player == null)
            return 0F;
        return (float) player.posX;
    }

    @Override
    public float getY() {
        ClientPlayerEntity player = this.player.get();
        if (player == null)
            return 0F;
        return (float) player.posY;
    }

    @Override
    public float getZ() {
        ClientPlayerEntity player = this.player.get();
        if (player == null)
            return 0F;
        return (float) player.posZ;
    }

    @Nonnull
    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.NONE;
    }

    @Override
    public void tick() {
        ticks++;
        volume = Math.max(0.15F, volume - minusPerTick);
    }
}
