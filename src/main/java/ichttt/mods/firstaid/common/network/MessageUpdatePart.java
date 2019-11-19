package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class MessageUpdatePart implements IMessage {
    private byte id;
    private int maxHealth;
    private float absorption;
    private float currentHealth;

    public MessageUpdatePart(AbstractDamageablePart part) {
        this.id = part.part.id;
        this.maxHealth = part.getMaxHealth();
        this.absorption = part.getAbsorption();
        this.currentHealth = part.currentHealth;
        validate();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = buf.readByte();
        this.maxHealth = buf.readInt();
        this.absorption = buf.readFloat();
        this.currentHealth = buf.readFloat();
        validate();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(id);
        buf.writeInt(maxHealth);
        buf.writeFloat(absorption);
        buf.writeFloat(currentHealth);
    }

    private void validate() {
        if (currentHealth < 0)
            throw new RuntimeException("Negative currentHealth!");
        if (absorption < 0)
            throw new RuntimeException("Negative absorption!");
        if (maxHealth < 0)
            throw new RuntimeException("Negative maxHealth!");
        if (EnumPlayerPart.fromID(id).id != this.id)
            throw new RuntimeException("Wrong player mapping!");
    }

    public static class Handler implements IMessageHandler<MessageUpdatePart, IMessage> {

        @Override
        public IMessage onMessage(MessageUpdatePart message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                AbstractDamageablePart damageablePart = Objects.requireNonNull(mc.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)).getFromEnum(EnumPlayerPart.fromID(message.id));
                damageablePart.setMaxHealth(message.maxHealth);
                damageablePart.setAbsorption(message.absorption);
                damageablePart.currentHealth = message.currentHealth;
            });
            return null;
        }
    }
}
