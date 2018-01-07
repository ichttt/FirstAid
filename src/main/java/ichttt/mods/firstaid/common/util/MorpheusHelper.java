package ichttt.mods.firstaid.common.util;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.quetzi.morpheus.Morpheus;
import net.quetzi.morpheus.MorpheusRegistry;
import net.quetzi.morpheus.api.INewDayHandler;
import net.quetzi.morpheus.world.WorldSleepState;

public class MorpheusHelper implements INewDayHandler {
    private static final MorpheusHelper INSTANCE = new MorpheusHelper();
    private INewDayHandler oldHandler;

    public static void register() {
        if (INSTANCE.oldHandler != null) throw new IllegalStateException("MorpheusHelper did already init!");
        INSTANCE.oldHandler = MorpheusRegistry.registry.get(0);
        Morpheus.register.registerHandler(INSTANCE, 0);
    }

    @Override
    public void startNewDay() {
        if (oldHandler != null) oldHandler.startNewDay(); //Start the new day

        if (FirstAidConfig.externalHealing.sleepHealing == 0F) return;

        WorldSleepState sleepState = Morpheus.playerSleepStatus.get(0);
        if (sleepState == null) return;

        if (areEnoughPlayersAsleep(sleepState)) {
            World world = DimensionManager.getWorld(0);
            for (EntityPlayer player : world.playerEntities) {
                if (player.isPlayerFullyAsleep()) {
                    HealthDistribution.distributeHealth(FirstAid.activeHealingConfig.sleepHealing, player, true); //heal the player who did sleep
                }
            }
        }
    }

    //See SleepChecker#areEnoughPlayersAsleep()
    private static boolean areEnoughPlayersAsleep(WorldSleepState sleepState) {
        return sleepState.getSleepingPlayers() > 0 && sleepState.getPercentSleeping() >= Morpheus.perc;
    }
}
