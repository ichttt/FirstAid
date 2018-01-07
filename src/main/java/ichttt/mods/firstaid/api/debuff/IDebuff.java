package ichttt.mods.firstaid.api.debuff;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IDebuff {

    void handleDamageTaken(float damage, float healthPerMax, EntityPlayerMP player);

    void handleHealing(float healingDone, float healthPerMax, EntityPlayerMP player);

    default boolean isEnabled() {
        return true;
    }

    default void update(EntityPlayer player) {}

    default void update(EntityPlayer player, float healthPerMax) {
        update(player);
    }
}
