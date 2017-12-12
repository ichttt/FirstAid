package ichttt.mods.firstaid.common.util;

import com.creativemd.playerrevive.api.IRevival;
import com.creativemd.playerrevive.api.capability.CapaRevive;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public class CommonUtils {

    public static void killPlayer(EntityPlayer player) {
        IRevival revival = getRevivalIfPossible(player);
        if (revival != null) {
            revival.startBleeding();
        } else
            ((DataManagerWrapper) player.dataManager).set_impl(EntityPlayer.HEALTH, 0F);
    }

    /**
     * Gets the cap, or null if not applicable
     * @param player The player to check
     * @return The cap or null if the player cannot be revived
     */
    @Nullable
    public static IRevival getRevivalIfPossible(@Nullable EntityPlayer player) {
        if (player == null || CapaRevive.reviveCapa == null)
            return null;
        MinecraftServer server = player.getServer();
        if (server == null)
            return null;
        IRevival revival = player.getCapability(CapaRevive.reviveCapa, null);
        if (revival != null && server.getPlayerList().getCurrentPlayerCount() > 1)
            return revival;
        else
            return null;
    }
}
