package de.technikforlife.firstaid.network;

import de.technikforlife.firstaid.damagesystem.capability.PlayerDataManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageApplyAbsorption implements IMessage {
    private float amount;

    public MessageApplyAbsorption() {}

    public MessageApplyAbsorption(float amount) {
        this.amount = amount;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        amount = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(amount);
    }

    public static class Handler implements IMessageHandler<MessageApplyAbsorption, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MessageApplyAbsorption message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> PlayerDataManager.getDamageModel(Minecraft.getMinecraft().player).setAbsorption(message.amount));
            return null;
        }
    }
}
