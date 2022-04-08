/*
 * FirstAid
 * Copyright (C) 2017-2022
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
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;

public class DebuffTimedSound implements TickableSoundInstance {
    private static final float volumeMultiplier = 1.25F;
    private final float minusPerTick;
    private final int debuffDuration;
    private final ResourceLocation soundLocation;
    private final SoundEvent event;
    private final WeakReference<LocalPlayer> player;
    private Sound sound;
    private float volume = volumeMultiplier;
    private int ticks;
    private final static Map<SoundEvent, DebuffTimedSound> activeSounds = new HashMap<>();

    public static void playHurtSound(SoundEvent event, int duration) {
        if (!FirstAidConfig.CLIENT.enableSounds.get())
            return;
        SoundManager soundHandler = Minecraft.getInstance().getSoundManager();
        DebuffTimedSound matchingSound = activeSounds.get(event);
        if (matchingSound != null) {
            if (!matchingSound.isStopped())
                soundHandler.stop(matchingSound);
            activeSounds.remove(event);
        }
        DebuffTimedSound newSound = new DebuffTimedSound(event, duration);
        soundHandler.play(newSound);
        activeSounds.put(event, newSound);
    }

    public DebuffTimedSound(SoundEvent event, int debuffDuration) {
        this.event = event;
        this.soundLocation = event.getLocation();
        this.player = new WeakReference<>(Minecraft.getInstance().player);
        this.debuffDuration = Integer.min(15 * 20, debuffDuration);
        this.minusPerTick = (1F / this.debuffDuration) * volumeMultiplier;
    }

    @Override
    public boolean isStopped() {
        LocalPlayer player = this.player.get();
        boolean done = player == null || ticks >= debuffDuration || player.getHealth() <= 0;
        if (done)
            activeSounds.remove(this.event);
        return done;
    }

    @Nonnull
    @Override
    public ResourceLocation getLocation() {
        return soundLocation;
    }

    @Nullable
    @Override
    public WeighedSoundEvents resolve(@Nonnull SoundManager handler) {
        WeighedSoundEvents soundEventAccessor = handler.getSoundEvent(this.soundLocation);

        if (soundEventAccessor == null)
        {
            FirstAid.LOGGER.warn("Missing sound for location " + this.soundLocation);
            this.sound = SoundManager.EMPTY_SOUND;
        }
        else
        {
            this.sound = soundEventAccessor.getSound();
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
    public SoundSource getSource() {
        return SoundSource.PLAYERS;
    }

    @Override
    public boolean isLooping() {
        return false;
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public int getDelay() {
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
    public double getX() {
        LocalPlayer player = this.player.get();
        if (player == null)
            return 0F;
        return player.getX();
    }

    @Override
    public double getY() {
        LocalPlayer player = this.player.get();
        if (player == null)
            return 0F;
        return player.getY();
    }

    @Override
    public double getZ() {
        LocalPlayer player = this.player.get();
        if (player == null)
            return 0F;
        return player.getZ();
    }

    @Nonnull
    @Override
    public Attenuation getAttenuation() {
        return Attenuation.NONE;
    }

    @Override
    public void tick() {
        ticks++;
        volume = Math.max(0.15F, volume - minusPerTick);
    }
}
