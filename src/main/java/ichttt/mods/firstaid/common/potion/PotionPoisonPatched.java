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

package ichttt.mods.firstaid.common.potion;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
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
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class PotionPoisonPatched extends Effect {
    public static final PotionPoisonPatched INSTANCE = new PotionPoisonPatched(EffectType.HARMFUL, 5149489);
    private static final Method method = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_184581_c", DamageSource.class);

    protected PotionPoisonPatched(EffectType type, int liquidColorIn) {
        super(type, liquidColorIn);
        this.setRegistryName(new ResourceLocation("minecraft", "poison"));
        FirstAid.LOGGER.info("Don't worry, the minecraft poison override IS intended.");
    }

    @Override
    public void performEffect(@Nonnull LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity && !(entity instanceof FakePlayer)) {
            if (!entity.isAlive() || entity.isInvulnerableTo(DamageSource.MAGIC))
                return;
            if (entity.world.isRemote) {
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel((PlayerEntity) entity);
                boolean playSound = false;
                for (AbstractDamageablePart part : damageModel) {
                    playSound = part.currentHealth > (part.canCauseDeath ? 1F : 0F);
                    if (playSound)
                        break;
                }
                if (playSound) {
                    try {
                        method.invoke(entity, DamageSource.MAGIC);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        FirstAid.LOGGER.warn("Could not play hurt sound!", e);
                    }
                }
            } else {
                if (entity.isSleeping())
                    entity.wakeUp();
                PlayerEntity player = (PlayerEntity) entity;
                AbstractPlayerDamageModel playerDamageModel = CommonUtils.getDamageModel(player);
                DamageDistribution.handleDamageTaken(RandomDamageDistribution.ANY_NOKILL, playerDamageModel, 1.0F, player, DamageSource.MAGIC, true, false);
            }
        }
        else {
            super.performEffect(entity, amplifier);
        }
    }
}
