package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class MessageSyncDamageModel implements IMessage {
    private NBTTagCompound playerDamageModel;

    public MessageSyncDamageModel() {}

    public MessageSyncDamageModel(AbstractPlayerDamageModel damageModel) {
        this.playerDamageModel = damageModel.serializeNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerDamageModel = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.playerDamageModel);
    }

    public static final class Handler implements IMessageHandler<MessageSyncDamageModel, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MessageSyncDamageModel message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> Objects.requireNonNull(mc.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)).deserializeNBT(message.playerDamageModel));
            return null;
        }
    }
}
