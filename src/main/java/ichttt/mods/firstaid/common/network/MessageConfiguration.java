/*
 * FirstAid
 * Copyright (C) 2017-2018
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

package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.ClientProxy;
import ichttt.mods.firstaid.client.HUDHandler;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageConfiguration {

    private NBTTagCompound playerDamageModel;

    public MessageConfiguration(NBTTagCompound model) {
        this.playerDamageModel = model;
    }

    public MessageConfiguration(PacketBuffer buffer) {
        this(buffer.readCompoundTag());
    }

    public void encode(PacketBuffer buf) {
        buf.writeCompoundTag(playerDamageModel);
    }

    public static class Handler {

        public static void onMessage(MessageConfiguration message, Supplier<NetworkEvent.Context> supplier) {
            Minecraft mc = Minecraft.getInstance();

            FirstAid.LOGGER.info("Received remote damage model");
            mc.addScheduledTask(() -> {
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(mc.player);
                damageModel.deserializeNBT(message.playerDamageModel);
                if (damageModel.hasTutorial)
                    CapProvider.tutorialDone.add(mc.player.getName().getString());
                else
                    mc.player.sendMessage(new TextComponentString("[First Aid] " + I18n.format("firstaid.tutorial.hint", ClientProxy.showWounds.getKey().getName())));
                HUDHandler.INSTANCE.ticker = 200;
                FirstAid.isSynced = true;
            });
        }
    }
}
