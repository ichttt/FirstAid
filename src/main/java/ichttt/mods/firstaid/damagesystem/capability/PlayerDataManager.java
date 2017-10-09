package ichttt.mods.firstaid.damagesystem.capability;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class PlayerDataManager {
    public static final ConcurrentMap<EntityPlayer, PlayerDamageModel> capList = new MapMaker().concurrencyLevel(2).weakKeys().makeMap();

    public static void tickPlayer(@Nonnull EntityPlayer player) {
        Preconditions.checkNotNull(player, "Player may not be null");
        capList.get(player).tick(player.world, player);
    }

    public static void clearPlayer(@Nonnull EntityPlayer player) {
        Preconditions.checkNotNull(player, "Player may not be null");
        capList.remove(player);
        capList.put(player, PlayerDamageModel.create());
    }

    public static PlayerDamageModel getDamageModel(@Nonnull EntityPlayer player) {
        Preconditions.checkNotNull(player, "Player may not be null");
        return capList.get(player);
    }
}
