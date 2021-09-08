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

package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.ClientHooks;
import ichttt.mods.firstaid.client.HUDHandler;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageConfiguration {

    private final CompoundTag playerDamageModel;

    public MessageConfiguration(CompoundTag model) {
        this.playerDamageModel = model;
    }

    public MessageConfiguration(FriendlyByteBuf buffer) {
        this(buffer.readNbt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(playerDamageModel);
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
                    Minecraft.getInstance().player.sendMessage(new TextComponent("[First Aid] " + I18n.get("firstaid.tutorial.hint", ClientHooks.SHOW_WOUNDS.getTranslatedKeyMessage().getString())), Util.NIL_UUID);
                HUDHandler.INSTANCE.ticker = 200;
                FirstAid.isSynced = true;
            });
        }
    }
}
