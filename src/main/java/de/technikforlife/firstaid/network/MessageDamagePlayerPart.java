package de.technikforlife.firstaid.network;

import de.technikforlife.firstaid.damagesystem.EnumPlayerPart;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;
import java.util.UUID;

public class MessageDamagePlayerPart implements IMessage {
    private EnumPlayerPart playerPart;
    private float amount;

    public MessageDamagePlayerPart() {

    }

    public MessageDamagePlayerPart(EnumPlayerPart part, float amount) {
        this.playerPart = part;
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
       this.playerPart = EnumPlayerPart.fromID(buf.readByte());
       this.amount = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(playerPart.id);
        buf.writeFloat(amount);
    }

    public static final class Handler implements IMessageHandler<MessageDamagePlayerPart, IMessage> {

        @Override
        public IMessage onMessage(MessageDamagePlayerPart message, MessageContext ctx) {
            PlayerDamageModel model = Objects.requireNonNull(Minecraft.getMinecraft().player.getCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null));
            model.getFromEnum(message.playerPart).damage(message.amount);
            return null;
        }
    }
}
