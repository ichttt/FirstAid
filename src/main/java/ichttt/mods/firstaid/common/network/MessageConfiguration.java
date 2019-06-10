/*
 * FirstAid
 * Copyright (C) 2017-2019
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
import ichttt.mods.firstaid.client.ClientHooks;
import ichttt.mods.firstaid.client.HUDHandler;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageConfiguration {

    private CompoundNBT playerDamageModel;

    public MessageConfiguration(CompoundNBT model) {
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
            NetworkEvent.Context ctx = supplier.get();
            CommonUtils.checkClient(ctx);

            FirstAid.LOGGER.info("Received remote damage model");
            ctx.enqueueWork(() -> {
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(Minecraft.getInstance().player);
                damageModel.deserializeNBT(message.playerDamageModel);
                if (damageModel.hasTutorial)
                    CapProvider.tutorialDone.add(Minecraft.getInstance().player.getName().getString());
                else
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent("[First Aid] " + I18n.format("firstaid.tutorial.hint", ClientHooks.showWounds.getKey().getName())));
                HUDHandler.INSTANCE.ticker = 200;
                FirstAid.isSynced = true;
            });
        }
    }
}
