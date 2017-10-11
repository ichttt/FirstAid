package ichttt.mods.firstaid.util;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

public class DimensionHandler { //TODO test
    private static final String NATURAL_REGENERATION = "naturalRegeneration";
    private static final Int2BooleanMap worldToNaturalRegeneration = new Int2BooleanOpenHashMap();

    private static void handleDim(WorldServer world, int id) {
        GameRules rules = world.getGameRules();
        worldToNaturalRegeneration.put(id, rules.getBoolean(NATURAL_REGENERATION));
        rules.setOrCreateGameRule(NATURAL_REGENERATION, Boolean.toString(FirstAidConfig.externalHealing.allowNaturalRegeneration));
    }

    public static void onLoad() {
        for (int id : DimensionManager.getIDs()) {
            WorldServer world = DimensionManager.getWorld(id);
            handleDim(world, id);
        }
        FirstAid.logger.info("Natural regeneration has been set to {}", FirstAidConfig.externalHealing.allowNaturalRegeneration);
    }

    public static void checkHandled(@Nonnull WorldServer world) {
        //noinspection ConstantConditions
        for (Int2BooleanMap.Entry entry : worldToNaturalRegeneration.int2BooleanEntrySet()) {
            int id = entry.getIntKey();
            WorldServer worldServer = DimensionManager.getWorld(id);
            if (worldServer == world)
                return;
        }
        FirstAid.logger.info("Handling previously unknown world {} ({})", world, world.getWorldInfo().getWorldName());
        Optional<Integer> id = Arrays.stream(DimensionManager.getIDs()).filter(integer -> DimensionManager.getWorld(integer) == world).findAny();
        if (id.isPresent())
            handleDim(world, id.get());
        else
            FirstAid.logger.error("Could not find ID for world {}, this should not happen!", world);
    }

    public static void revert() {
        for (Int2BooleanMap.Entry entry : worldToNaturalRegeneration.int2BooleanEntrySet()) {
            int id = entry.getIntKey();
            WorldServer worldServer = DimensionManager.getWorld(id);
            if (worldServer != null)
                worldServer.getGameRules().setOrCreateGameRule(NATURAL_REGENERATION, Boolean.toString(entry.getBooleanValue()));
            else
                FirstAid.logger.warn("Failed to revert natural regeneration for world with id " + id);
        }
        FirstAid.logger.info("Restored original naturalRegeneration values for worlds");
    }

    public static void clear() {
        worldToNaturalRegeneration.clear();
    }
}
