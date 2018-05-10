package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.common.CapProvider;
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
                CapProvider.tutorialDone.add(player.getName());
                Objects.requireNonNull(player.getServer()).addScheduledTask(() -> Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)).hasTutorial = true);
            } else if (message.type == Type.REQUEST_REFRESH) {
                FirstAid.NETWORKING.sendTo(new MessageResync(Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null))), player);
            }
            return null;
        }
    }
}
