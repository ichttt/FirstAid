package ichttt.mods.firstaid.common.damagesystem.debuff;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IDebuff {

    void handleDamageTaken(float damage, float healthPerMax, EntityPlayerMP player);

    void handleHealing(float healingDone, float healthPerMax, EntityPlayerMP player);
}
