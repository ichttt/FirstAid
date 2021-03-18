/*
 * FirstAid
 * Copyright (C) 2017-2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*package ichttt.mods.firstaid.common.util; TODO Morpheus compat

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
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
        FirstAid.LOGGER.info("Morpheus present - enabling compatibility module. Parent: " + INSTANCE.oldHandler.getClass());
        Morpheus.register.registerHandler(INSTANCE, 0);
    }

    @Override
    public void startNewDay() {
        if (oldHandler != null)
            oldHandler.startNewDay(); //Start the new day

        if (FirstAidConfig.SERVER.sleepHealPercentage.get() <= 0D)
            return;

        WorldSleepState sleepState = Morpheus.playerSleepStatus.get(0);
        if (sleepState == null)
            return;

        World world = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD);
        for (PlayerEntity player : world.getPlayers()) {
            if (player.isPlayerFullyAsleep()) {
                CommonUtils.getDamageModel(player).sleepHeal(player);
            }
        }
    }
}*/
