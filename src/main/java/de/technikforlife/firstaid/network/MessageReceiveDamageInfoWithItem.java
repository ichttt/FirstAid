package de.technikforlife.firstaid.network;

import de.technikforlife.firstaid.client.GuiApplyHealthItem;
import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageReceiveDamageInfoWithItem implements IMessage {
    private NBTTagCompound playerDamageModel;
    private EnumHealingType healingType;
    private EnumHand hand;

    public MessageReceiveDamageInfoWithItem() {}

    public MessageReceiveDamageInfoWithItem(PlayerDamageModel model, EnumHealingType healingType, EnumHand hand) {
        this.playerDamageModel = model.serializeNBT();
        this.healingType = healingType;
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerDamageModel = ByteBufUtils.readTag(buf);
        healingType = EnumHealingType.fromID(buf.readByte());
        hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, playerDamageModel);
        buf.writeByte(healingType.id);
        buf.writeBoolean(hand == EnumHand.MAIN_HAND);
    }

    public static class Handler implements IMessageHandler<MessageReceiveDamageInfoWithItem, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageReceiveDamageInfoWithItem message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                PlayerDamageModel damageModel = new PlayerDamageModel();
                damageModel.deserializeNBT(message.playerDamageModel);
                if (GuiApplyHealthItem.INSTANCE != null) //GUI already closed
                    GuiApplyHealthItem.INSTANCE.onReceiveData(damageModel, message.healingType, message.hand);
            });
            return null;
        }
    }
}
