/*
 * FirstAid
 * Copyright (C) 2017-2020
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
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.ClientProxy;
import ichttt.mods.firstaid.client.HUDHandler;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.config.ConfigEntry;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class MessageConfiguration implements IMessage {

    private NBTTagCompound playerDamageModel;
    private boolean syncConfig;

    public MessageConfiguration() {}

    public MessageConfiguration(AbstractPlayerDamageModel model, boolean syncConfig) {
        this.playerDamageModel = model.serializeNBT();
        this.syncConfig = syncConfig;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerDamageModel = ByteBufUtils.readTag(buf);

        syncConfig = buf.readBoolean();
        if (syncConfig) {
            for (ConfigEntry<ExtraConfig.Sync> entry : FirstAid.syncedConfigOptions)
                entry.readFromBuf(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, playerDamageModel);

        buf.writeBoolean(syncConfig);
        if (syncConfig) {
            for (ConfigEntry<ExtraConfig.Sync> entry : FirstAid.syncedConfigOptions)
                entry.writeToBuf(buf);
        }
    }

    public static class Handler implements IMessageHandler<MessageConfiguration, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageConfiguration message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();

            FirstAid.LOGGER.info(message.syncConfig ? "Received remote damage model and config" : "Received remote damage model");
            mc.addScheduledTask(() -> {
                AbstractPlayerDamageModel damageModel = Objects.requireNonNull(mc.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
                damageModel.deserializeNBT(message.playerDamageModel);
                if (damageModel.hasTutorial)
                    CapProvider.tutorialDone.add(mc.player.getName());
                else
                    mc.player.sendMessage(new TextComponentString("[First Aid] " + I18n.format("firstaid.tutorial.hint", ClientProxy.showWounds.getDisplayName())));
                HUDHandler.INSTANCE.ticker = 200;
                FirstAid.isSynced = true;
            });
            return null;
        }
    }
}
