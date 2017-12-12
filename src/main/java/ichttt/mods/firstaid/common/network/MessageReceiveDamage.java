package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class MessageReceiveDamage implements IMessage {

    private EnumPlayerPart part;
    private float damageAmount;
    private float minHealth;

    public MessageReceiveDamage() {}

    public MessageReceiveDamage(EnumPlayerPart part, float damageAmount, float minHealth) {
        this.part = part;
        this.damageAmount = damageAmount;
        this.minHealth = minHealth;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        part = EnumPlayerPart.fromID(buf.readByte());
        damageAmount = buf.readFloat();
        minHealth = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(part.id);
        buf.writeFloat(damageAmount);
        buf.writeFloat(minHealth);
    }

    public static class Handler implements IMessageHandler<MessageReceiveDamage, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageReceiveDamage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
               AbstractPlayerDamageModel damageModel = PlayerDataManager.getDamageModel(Minecraft.getMinecraft().player);
                Objects.requireNonNull(damageModel);
                AbstractDamageablePart part = damageModel.getFromEnum(message.part);
                part.damage(message.damageAmount, null, false, message.minHealth);
            });
            return null;
        }
    }
}
