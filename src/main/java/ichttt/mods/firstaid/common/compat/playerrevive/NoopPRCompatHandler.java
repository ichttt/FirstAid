package ichttt.mods.firstaid.common.compat.playerrevive;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public class NoopPRCompatHandler implements IPRCompatHandler {

    @Override
    public boolean tryRevivePlayer(Player player, DamageSource source) {
        return false;
    }

    @Override
    public boolean isBleeding(Player player) {
        return false;
    }
}
