package ichttt.mods.firstaid.network;

import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.damagesystem.distribution.HealthDistribution;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageAddHealth implements IMessage {
    private float amount;

    public MessageAddHealth() {}

    public MessageAddHealth(float amount) {
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.amount = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(this.amount);
    }

    public static class Handler implements IMessageHandler<MessageAddHealth, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MessageAddHealth message, MessageContext ctx) {
            EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
            float amount = message.amount;
            if (amount == Float.MAX_VALUE) //short cut
                PlayerDataManager.getDamageModel(playerSP).forEach(damageablePart -> damageablePart.currentHealth = damageablePart.getMaxHealth());
            else
                HealthDistribution.distributeHealth(message.amount, playerSP);
            return null;
        }
    }
}
