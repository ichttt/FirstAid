package ichttt.mods.firstaid.common.compat.playerrevive;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.playerrevive.api.event.PlayerBleedOutEvent;
import team.creative.playerrevive.api.event.PlayerRevivedEvent;

public class PlayerReviveEventHandler {

    @SubscribeEvent
    public static void onPlayerRevived(PlayerRevivedEvent event) {
        Player player = event.getPlayer();
        LazyOptional<AbstractPlayerDamageModel> damageModel = CommonUtils.getOptionalDamageModel(player);
        damageModel.ifPresent(model -> model.revivePlayer(player));
    }

}
