package ichttt.mods.firstaid.network;

import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageReceiveDamageModel implements IMessage {
    private NBTTagCompound playerDamageModel;

    public MessageReceiveDamageModel() {}

    public MessageReceiveDamageModel(PlayerDamageModel model) {
        this.playerDamageModel = model.serializeNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerDamageModel = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, playerDamageModel);
    }

    public static class Handler implements IMessageHandler<MessageReceiveDamageModel, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageReceiveDamageModel message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                PlayerDamageModel damageModel = new PlayerDamageModel();
                damageModel.deserializeNBT(message.playerDamageModel);
                PlayerDataManager.capList.remove(Minecraft.getMinecraft().player);
                PlayerDataManager.capList.put(Minecraft.getMinecraft().player, damageModel);
            });
            return null;
        }
    }
}
