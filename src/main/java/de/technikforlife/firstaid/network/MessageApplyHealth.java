package de.technikforlife.firstaid.network;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.DamageablePart;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.PlayerDataManager;
import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;
import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import de.technikforlife.firstaid.items.ItemHealing;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
        public IMessage onMessage(final MessageApplyHealth message, final MessageContext ctx) {
            //noinspection ConstantConditions
            ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                PlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
                ItemStack stack = player.getHeldItem(message.hand);
                Item item = stack.getItem();
                if (!(item instanceof ItemHealing)) {
                    FirstAid.logger.warn("Player {} has invalid item in hand {} while it should be an healing item", player.getName(), item.getUnlocalizedName());
                    player.sendMessage(new TextComponentString("Unable to apply healing item!"));
                    return;
                } else {
                    ItemHealing itemHealing = (ItemHealing) item;
                    if (itemHealing.type != message.healingType) {
                        FirstAid.logger.warn("Player {} has invalid item with type {} in hand while it should be {}", player.getName(), itemHealing.type, message.healingType);
                        player.sendMessage(new TextComponentString("Unable to apply healing item!"));
                        return;
                    }
                }
                stack.shrink(1);
                DamageablePart damageablePart = damageModel.getFromEnum(message.part);
                damageablePart.applyItem(message.healingType.createNewHealer());
            });
            return null;
        }
    }
}
