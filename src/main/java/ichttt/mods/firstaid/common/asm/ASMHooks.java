package ichttt.mods.firstaid.common.asm;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.FakePlayer;

@SuppressWarnings("unused")
public class ASMHooks {

    public static void onPoisonEffect(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && !(entity instanceof FakePlayer)) {
            if (entity.world.isRemote)
                return;
            EntityPlayer player = (EntityPlayer) entity;
            AbstractPlayerDamageModel playerDamageModel = PlayerDataManager.getDamageModel(player);
            if (playerDamageModel.HEAD.currentHealth + playerDamageModel.BODY.currentHealth > 2) {
                DamageDistribution.handleDamageTaken(RandomDamageDistribution.ANY_NOKILL, playerDamageModel, 1.0F, player, DamageSource.MAGIC, true, false);
            }
        }
        else if (entity.getHealth() > 1.0F) {
            entity.attackEntityFrom(DamageSource.MAGIC, 1.0F);
        }
    }
}
