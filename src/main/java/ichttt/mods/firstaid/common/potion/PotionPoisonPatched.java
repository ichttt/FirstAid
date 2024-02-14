/*
 * FirstAid
 * Copyright (C) 2017-2024
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

package ichttt.mods.firstaid.common.potion;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class PotionPoisonPatched extends MobEffect {
    private static final Method getHurtSound = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_7975_", DamageSource.class);
    private static final Method getSoundVolume = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_6121_");
    private static final Method getVoicePitch = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_6100_");

    public PotionPoisonPatched(MobEffectCategory type, int liquidColorIn) {
        super(type, liquidColorIn);
    }

    @Override
    public void applyEffectTick(@Nonnull LivingEntity entity, int amplifier) {
        if (entity instanceof Player && !(entity instanceof FakePlayer) && (FirstAidConfig.SERVER.causeDeathBody.get() || FirstAidConfig.SERVER.causeDeathHead.get())) {
            if (entity.level().isClientSide || !entity.isAlive() || entity.isInvulnerableTo(entity.damageSources().magic()))
                return;
            if (entity.isSleeping())
                entity.stopSleeping();
            Player player = (Player) entity;
            AbstractPlayerDamageModel playerDamageModel = CommonUtils.getDamageModel(player);
            if (DamageDistribution.handleDamageTaken(RandomDamageDistributionAlgorithm.ANY_NOKILL, playerDamageModel, 1.0F, player, entity.damageSources().magic(), true, false) != 1.0F) {
                try {
                    SoundEvent sound = (SoundEvent) getHurtSound.invoke(player, entity.damageSources().magic());
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, player.getSoundSource(), (float) getSoundVolume.invoke(player), (float) getVoicePitch.invoke(player));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    FirstAid.LOGGER.error("Could not play hurt sound!", e);
                }
            }
        }
        else {
            super.applyEffectTick(entity, amplifier);
        }
    }
}
