package de.technikforlife.firstaid.damagesystem.capability;

import com.google.common.collect.MapMaker;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.ConcurrentMap;

public class PlayerDataManager {
    public static final ConcurrentMap<EntityPlayer, PlayerDamageModel> capList = new MapMaker().concurrencyLevel(2).weakKeys().makeMap();

    public static void clearData() {
        capList.clear();
    }

    public static void tickPlayer(EntityPlayer player) {
        capList.get(player).tick(player.world, player);
    }

    public static void clearPlayer(EntityPlayer player) {
        capList.remove(player);
        capList.put(player, new PlayerDamageModel());
    }
}
