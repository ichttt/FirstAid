package ichttt.mods.firstaid.network;

import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageHasTutorial implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) { }

    @Override
    public void toBytes(ByteBuf buf) { }

    public static class Handler implements IMessageHandler<MessageHasTutorial, IMessage> {

        @Override
        public IMessage onMessage(MessageHasTutorial message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            //noinspection ConstantConditions
            player.getServer().addScheduledTask(() -> PlayerDataManager.getDamageModel(player).hasTutorial = true);
            return null;
        }
    }
}
