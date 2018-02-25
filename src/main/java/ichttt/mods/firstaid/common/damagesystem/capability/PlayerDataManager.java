package ichttt.mods.firstaid.common.damagesystem.capability;

import com.google.common.collect.MapMaker;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class PlayerDataManager {
    public static final ConcurrentMap<EntityPlayer, AbstractPlayerDamageModel> capList = new MapMaker().concurrencyLevel(2).weakKeys().makeMap();
    public static final Set<String> tutorialDone = new HashSet<>();

    public static void tickPlayer(@Nonnull EntityPlayer player) {
        Objects.requireNonNull(player, "Player may not be null");
        capList.get(player).tick(player.world, player);
    }

    public static AbstractPlayerDamageModel getDamageModel(@Nonnull EntityPlayer player) {
        Objects.requireNonNull(player, "Player may not be null");
        return capList.get(player);
    }

    public static void put(EntityPlayer player, AbstractPlayerDamageModel damageModel) {
        Objects.requireNonNull(player, "Player may not be null");
        capList.put(player, damageModel);
    }

    public static void clear() {
        capList.clear();
        tutorialDone.clear();
    }

    public static void resetPlayer(EntityPlayer player) {
        Objects.requireNonNull(player, "Player may not be null");
        capList.remove(player);
        capList.put(player, FirstAid.isSynced ? PlayerDamageModel.create() : PlayerDamageModel.createTemp());
    }
}
