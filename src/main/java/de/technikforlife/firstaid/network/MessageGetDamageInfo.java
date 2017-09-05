package de.technikforlife.firstaid.network;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageGetDamageInfo implements IMessage {

    public MessageGetDamageInfo() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageGetDamageInfo, IMessage> {

        @Override
        public MessageReceiveDamageInfo onMessage(MessageGetDamageInfo message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            //noinspection ConstantConditions
            player.getServer().addScheduledTask(() ->
                    FirstAid.NETWORKING.sendTo(new MessageReceiveDamageInfo(player.getCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null)), player));
            return null;
        }
    }
}
