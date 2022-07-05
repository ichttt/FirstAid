package ichttt.mods.firstaid.common.compat.playerrevive;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public interface IPRCompatHandler {

    /**
     * Should be called when the played is about to be killed
     *
     * @return True if the player can still be revived and should not be killed,
     * false if the health should be set to 0 and the player should be killed.
     */
    boolean tryRevivePlayer(Player player, DamageSource source);

    boolean isBleeding(Player player);
}
