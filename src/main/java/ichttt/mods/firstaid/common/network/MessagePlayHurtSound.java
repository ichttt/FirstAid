package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.common.EnumHurtSound;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessagePlayHurtSound implements IMessage {
    private EnumHurtSound sound;
    private int duration;

    public MessagePlayHurtSound () {}

    public MessagePlayHurtSound(EnumHurtSound sound, int duration) {
        this.sound = sound;
        this.duration = duration;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        sound = EnumHurtSound.values()[buf.readByte()];
        duration = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(sound.ordinal());
        buf.writeInt(duration);
    }

    public static class Handler implements IMessageHandler<MessagePlayHurtSound, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MessagePlayHurtSound message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> message.sound.playSound(message.duration));
            return null;
        }
    }
}
