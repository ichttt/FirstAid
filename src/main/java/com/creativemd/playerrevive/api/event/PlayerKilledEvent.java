package com.creativemd.playerrevive.api.event;

import com.creativemd.playerrevive.api.IRevival;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Fired before a player is killed
 */
public class PlayerKilledEvent extends PlayerEvent {
    private final IRevival revival;

    public PlayerKilledEvent(EntityPlayer player, IRevival revival) {
        super(player);
        this.revival = revival;
    }

    public IRevival getRevival() {
        return this.revival;
    }


}
