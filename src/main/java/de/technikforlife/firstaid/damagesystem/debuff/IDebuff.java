package de.technikforlife.firstaid.damagesystem.debuff;

import net.minecraft.entity.player.EntityPlayer;

public interface IDebuff {

    void handleDamageTaken(float damage, float healthPerMax, EntityPlayer player);

    void handleHealing(float healingDone, float healthPerMax, EntityPlayer player);
}
