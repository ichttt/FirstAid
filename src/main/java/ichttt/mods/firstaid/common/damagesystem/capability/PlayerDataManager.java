package ichttt.mods.firstaid.common.damagesystem.capability;

import com.google.common.collect.MapMaker;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlayerDataManager {
    public static final ConcurrentMap<EntityPlayer, AbstractPlayerDamageModel> capList = new MapMaker().concurrencyLevel(2).weakKeys().makeMap();
    public static final Set<String> tutorialDone = Collections.newSetFromMap(new WeakHashMap<>());

    public static void tickPlayer(@Nonnull EntityPlayer player) {
        Objects.requireNonNull(player, "Player may not be null");
        capList.get(player).tick(player.world, player);
    }

    public static void clearPlayer(@Nonnull EntityPlayer player) {
        Objects.requireNonNull(player, "Player may not be null");
        capList.remove(player);
        capList.put(player, PlayerDamageModel.create());
    }

    public static AbstractPlayerDamageModel getDamageModel(@Nonnull EntityPlayer player) {
        Objects.requireNonNull(player, "Player may not be null");
        return capList.get(player);
    }
}
