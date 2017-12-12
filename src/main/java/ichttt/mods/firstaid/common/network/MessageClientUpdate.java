package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class MessageClientUpdate implements IMessage {
    private Type type;

    public MessageClientUpdate() {}

    public MessageClientUpdate(Type type) {
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = Type.TYPES[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(type.ordinal());
    }

    public enum Type {
        TUTORIAL_COMPLETE, REQUEST_REFRESH;

        private static final Type[] TYPES = values();
    }

    public static class Handler implements IMessageHandler<MessageClientUpdate, IMessage> {

        @Override
        public IMessage onMessage(MessageClientUpdate message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (message.type == Type.TUTORIAL_COMPLETE) {
                PlayerDataManager.tutorialDone.add(player.getName());
                Objects.requireNonNull(player.getServer()).addScheduledTask(() -> PlayerDataManager.getDamageModel(player).hasTutorial = true);
            } else if (message.type == Type.REQUEST_REFRESH) {
                FirstAid.NETWORKING.sendTo(new MessageResync(PlayerDataManager.getDamageModel(player)), player);
            }
            return null;
        }
    }
}
