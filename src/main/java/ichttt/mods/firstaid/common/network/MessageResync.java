package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageResync implements IMessage {
    private NBTTagCompound playerDamageModel;

    public MessageResync() {}

    public MessageResync(AbstractPlayerDamageModel damageModel) {
        this.playerDamageModel = damageModel.serializeNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerDamageModel = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.playerDamageModel);
    }

    public static final class Handler implements IMessageHandler<MessageResync, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MessageResync message, MessageContext ctx) {
            PlayerDamageModel damageModel = PlayerDamageModel.create();
            damageModel.deserializeNBT(message.playerDamageModel);
            PlayerDataManager.put(Minecraft.getMinecraft().player, damageModel);
            return null;
        }
    }
}
