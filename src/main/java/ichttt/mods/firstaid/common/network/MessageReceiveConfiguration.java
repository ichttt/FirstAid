package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.ClientProxy;
import ichttt.mods.firstaid.common.config.ConfigEntry;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import ichttt.mods.firstaid.common.config.ExtraConfigManager;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
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

public class MessageReceiveConfiguration implements IMessage {

    private NBTTagCompound playerDamageModel;

    public MessageReceiveConfiguration() {}

    public MessageReceiveConfiguration(AbstractPlayerDamageModel model) {
        this.playerDamageModel = model.serializeNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        for (ConfigEntry<ExtraConfig.Sync> entry : ExtraConfigManager.syncedConfigOptions)
            entry.readFromBuf(buf);

        playerDamageModel = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (ConfigEntry<ExtraConfig.Sync> entry : ExtraConfigManager.syncedConfigOptions)
            entry.writeToBuf(buf);

        ByteBufUtils.writeTag(buf, playerDamageModel);
    }

    public static class Handler implements IMessageHandler<MessageReceiveConfiguration, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageReceiveConfiguration message, MessageContext ctx) {
            AbstractPlayerDamageModel damageModel = PlayerDamageModel.create();
            damageModel.deserializeNBT(message.playerDamageModel);
            Minecraft mc = Minecraft.getMinecraft();

            FirstAid.logger.info("Received configuration");
            mc.addScheduledTask(() -> {
                FirstAid.isSynced = true;
                PlayerDataManager.put(mc.player, damageModel);
                if (damageModel.hasTutorial)
                    PlayerDataManager.tutorialDone.add(mc.player.getName());
                else
                    mc.player.sendMessage(new TextComponentString("[First Aid] " + I18n.format("firstaid.tutorial.hint", ClientProxy.showWounds.getDisplayName())));
            });
            return null;
        }
    }
}
