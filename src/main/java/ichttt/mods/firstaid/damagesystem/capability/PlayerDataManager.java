package ichttt.mods.firstaid.damagesystem.capability;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentMap;

public class PlayerDataManager {
    public static final ConcurrentMap<EntityPlayer, PlayerDamageModel> capList = new MapMaker().concurrencyLevel(2).weakKeys().makeMap();

    public static void tickPlayer(@Nonnull EntityPlayer player) {
        Preconditions.checkNotNull(player, "Player may not be null");
        capList.get(player).tick(player.world, player);
    }

    public static void clearPlayer(@Nonnull EntityPlayer player) {
        Preconditions.checkNotNull(player, "Player may not be null");
        capList.remove(player);
        capList.put(player, new PlayerDamageModel());
    }

    @Nonnull
    public static PlayerDamageModel getDamageModel(@Nonnull EntityPlayer player) {
        Preconditions.checkNotNull(player, "Player may not be null");
        return capList.computeIfAbsent(player, p -> new PlayerDamageModel());
    }
}
