package de.technikforlife.firstaid.network;

import de.technikforlife.firstaid.damagesystem.DamageablePart;
import de.technikforlife.firstaid.damagesystem.EnumHealingType;
import de.technikforlife.firstaid.damagesystem.EnumPlayerPart;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class MessageApplyHealth implements IMessage {
    private EnumPlayerPart part;
    private EnumHealingType healingType;

    public MessageApplyHealth() {}

    public MessageApplyHealth(EnumPlayerPart part, EnumHealingType healingType) {

        this.part = part;
        this.healingType = healingType;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        part = EnumPlayerPart.fromID(buf.readByte());
        healingType = EnumHealingType.fromID(buf.readByte());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(part.id);
        buf.writeByte(healingType.id);
    }

    public static class Handler implements IMessageHandler<MessageApplyHealth, IMessage> {

        @Override
        public IMessage onMessage(MessageApplyHealth message, MessageContext ctx) {
            PlayerDamageModel damageModel = Objects.requireNonNull(ctx.getServerHandler().player.getCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null));
            DamageablePart damageablePart = damageModel.getFromEnum(message.part);
            damageablePart.applyItem(message.healingType);
            return null;
        }
    }
}
