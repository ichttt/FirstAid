package de.technikforlife.firstaid.network;

import de.technikforlife.firstaid.client.GuiApplyHealthItem;
import de.technikforlife.firstaid.damagesystem.EnumHealingType;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageReceiveDamageInfo implements IMessage {
    private NBTTagCompound playerDamageModel;
    private EnumHealingType healingType;

    public MessageReceiveDamageInfo() {}

    public MessageReceiveDamageInfo(PlayerDamageModel model, EnumHealingType healingType) {
        this.playerDamageModel = model.serializeNBT();
        this.healingType = healingType;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerDamageModel = ByteBufUtils.readTag(buf);
        healingType = EnumHealingType.fromID(buf.readByte());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, playerDamageModel);
        buf.writeByte(healingType.id);
    }

    public static class Handler implements IMessageHandler<MessageReceiveDamageInfo, IMessage> {

        @Override
        public IMessage onMessage(MessageReceiveDamageInfo message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                PlayerDamageModel damageModel = new PlayerDamageModel(Minecraft.getMinecraft().player.getPersistentID());
                damageModel.deserializeNBT(message.playerDamageModel);
                Minecraft.getMinecraft().displayGuiScreen(new GuiApplyHealthItem(damageModel, message.healingType));
            });
            return null;
        }
    }
}
