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
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class PotionPoisonPatched extends Potion {
    public static final PotionPoisonPatched INSTANCE = new PotionPoisonPatched(true, 5149489);
    private static final Method getHurtSound = ObfuscationReflectionHelper.findMethod(EntityLivingBase.class, "func_184601_bQ", SoundEvent.class, DamageSource.class);
    private static final Method getSoundVolume = ObfuscationReflectionHelper.findMethod(EntityLivingBase.class, "func_70599_aP", float.class);
    private static final Method getSoundPitch = ObfuscationReflectionHelper.findMethod(EntityLivingBase.class, "func_70647_i", float.class);

    protected PotionPoisonPatched(boolean isBadEffectIn, int liquidColorIn) {
        super(isBadEffectIn, liquidColorIn);
        this.setPotionName("effect.poison");
        this.setIconIndex(6, 0);
        this.setEffectiveness(0.25D);
        this.setRegistryName(new ResourceLocation("minecraft", "poison"));
        FirstAid.LOGGER.info("Don't worry, the minecraft poison override IS intended.");
    }

    @Override
    public void performEffect(@Nonnull EntityLivingBase entity, int amplifier) {
        if (FirstAidConfig.damageSystem.causeDeathBody || FirstAidConfig.damageSystem.causeDeathHead) {
            if (entity.world.isRemote || entity.isDead || entity.isEntityInvulnerable(DamageSource.MAGIC))
                return;
            EntityDamageModel damageModel = entity.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
            if (damageModel == null) {
                super.performEffect(entity, amplifier);
                return;
            }
            if (DamageDistribution.handleDamageTaken(RandomDamageDistribution.ANY_NOKILL, damageModel, 1.0F, entity, DamageSource.MAGIC, true, false) != 1.0F) {
                if (entity.isPlayerSleeping() && entity instanceof EntityPlayer)
                    ((EntityPlayer)entity).wakeUpPlayer(true, true, false);
                try {
                    SoundEvent sound = (SoundEvent) getHurtSound.invoke(entity, DamageSource.MAGIC);
                    entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, sound, entity.getSoundCategory(), (float) getSoundVolume.invoke(entity), (float) getSoundPitch.invoke(entity));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    FirstAid.LOGGER.error("Could not play hurt sound!", e);
                }
            }
        }
        else {
            super.performEffect(entity, amplifier);
        }
    }
}
