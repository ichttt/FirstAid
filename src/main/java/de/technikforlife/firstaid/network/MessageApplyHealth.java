package de.technikforlife.firstaid.network;

import de.technikforlife.firstaid.damagesystem.DamageablePart;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;
import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class MessageApplyHealth implements IMessage {
    private EnumPlayerPart part;
    private EnumHealingType healingType;
    private EnumHand hand;

    public MessageApplyHealth() {}

    public MessageApplyHealth(EnumPlayerPart part, EnumHealingType healingType, EnumHand hand) {
        this.part = part;
        this.healingType = healingType;
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        part = EnumPlayerPart.fromID(buf.readByte());
        healingType = EnumHealingType.fromID(buf.readByte());
        hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(part.id);
        buf.writeByte(healingType.id);
        buf.writeBoolean(hand == EnumHand.MAIN_HAND);
    }

    public static class Handler implements IMessageHandler<MessageApplyHealth, IMessage> {

        @Override
        public IMessage onMessage(MessageApplyHealth message, MessageContext ctx) {
            //noinspection ConstantConditions
            ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
                PlayerDamageModel damageModel = Objects.requireNonNull(ctx.getServerHandler().player.getCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null));
                ctx.getServerHandler().player.getHeldItem(message.hand).shrink(1);
                DamageablePart damageablePart = damageModel.getFromEnum(message.part);
                damageablePart.applyItem(message.healingType.createNewHealer());
            });
            return null;
        }
    }
}
