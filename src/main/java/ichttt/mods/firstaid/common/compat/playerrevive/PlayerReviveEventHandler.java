/*
 * FirstAid
 * Copyright (C) 2017-2023
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

package ichttt.mods.firstaid.common.compat.playerrevive;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.SynchedEntityDataWrapper;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.playerrevive.api.event.PlayerBleedOutEvent;
import team.creative.playerrevive.api.event.PlayerRevivedEvent;

public class PlayerReviveEventHandler {

    @SubscribeEvent
    public static void onPlayerRevived(PlayerRevivedEvent event) {
        Player player = event.getEntity();

        SynchedEntityDataWrapper wrapper = (SynchedEntityDataWrapper) player.entityData;
        wrapper.toggleBeingRevived(false);

        LazyOptional<AbstractPlayerDamageModel> damageModel = CommonUtils.getOptionalDamageModel(player);
        damageModel.ifPresent(model -> model.revivePlayer(player));
    }

    @SubscribeEvent
    public static void onPlayerBleedOut(PlayerBleedOutEvent event) {
        Player player = event.getEntity();
        SynchedEntityDataWrapper wrapper = (SynchedEntityDataWrapper) player.entityData;
        wrapper.toggleBeingRevived(false);
    }

}
