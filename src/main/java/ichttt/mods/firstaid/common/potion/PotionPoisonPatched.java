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

package ichttt.mods.firstaid.common.potion;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class PotionPoisonPatched extends Effect {
    public static final PotionPoisonPatched INSTANCE = new PotionPoisonPatched(EffectType.HARMFUL, 5149489);
    private static final Method getHurtSound = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_184601_bQ", DamageSource.class);
    private static final Method getSoundVolume = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_70599_aP");
    private static final Method getSoundPitch = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_70647_i");

    protected PotionPoisonPatched(EffectType type, int liquidColorIn) {
        super(type, liquidColorIn);
        this.setRegistryName(new ResourceLocation("minecraft", "poison"));
        FirstAid.LOGGER.info("Don't worry, the minecraft poison override IS intended.");
    }

    @Override
    public void applyEffectTick(@Nonnull LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity && !(entity instanceof FakePlayer) && (FirstAidConfig.SERVER.causeDeathBody.get() || FirstAidConfig.SERVER.causeDeathHead.get())) {
            if (entity.level.isClientSide || !entity.isAlive() || entity.isInvulnerableTo(DamageSource.MAGIC))
                return;
            if (entity.isSleeping())
                entity.stopSleeping();
            PlayerEntity player = (PlayerEntity) entity;
            AbstractPlayerDamageModel playerDamageModel = CommonUtils.getDamageModel(player);
            if (DamageDistribution.handleDamageTaken(RandomDamageDistribution.ANY_NOKILL, playerDamageModel, 1.0F, player, DamageSource.MAGIC, true, false) != 1.0F) {
                try {
                    SoundEvent sound = (SoundEvent) getHurtSound.invoke(player, DamageSource.MAGIC);
                    player.level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, player.getSoundSource(), (float) getSoundVolume.invoke(player), (float) getSoundPitch.invoke(player));
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
