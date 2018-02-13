package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.ClientProxy;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageReceiveConfiguration implements IMessage {

    public static void validate() {
        if (FirstAidConfig.DamageSystem.class.getDeclaredFields().length != 8)
            throw new RuntimeException("ICHTTT HAS NOT UPDATED HIS PACKETS! GOT NEW FIELDS IN DAMAGE SYSTEM");
        if (FirstAidConfig.ExternalHealing.class.getDeclaredFields().length != 5) //this is nice, I would have forgotten this totally
            throw new RuntimeException("ICHTTT HAS NOT UPDATED HIS PACKETS! GOT NEW FIELDS IN HEALING SYSTEM");
    }

    private NBTTagCompound playerDamageModel;
    private FirstAidConfig.ExternalHealing healingCfg;
    private FirstAidConfig.DamageSystem damageCfg;
    private boolean scaleMaxHealth;
    private boolean capMaxHealth;

    public MessageReceiveConfiguration() {}

    public MessageReceiveConfiguration(AbstractPlayerDamageModel model, FirstAidConfig.ExternalHealing healingCfg, FirstAidConfig.DamageSystem damageCfg, boolean scaleMaxHealth, boolean capMaxHealth) {
        this.playerDamageModel = model.serializeNBT();
        this.healingCfg = healingCfg;
        this.damageCfg = damageCfg;
        this.scaleMaxHealth = scaleMaxHealth;
        this.capMaxHealth = capMaxHealth;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        healingCfg = new FirstAidConfig.ExternalHealing();
        healingCfg.allowNaturalRegeneration = buf.readBoolean();
        healingCfg.allowOtherHealingItems = buf.readBoolean();
        healingCfg.sleepHealing = buf.readFloat();
        healingCfg.otherRegenMultiplier = buf.readDouble();
        healingCfg.naturalRegenMultiplier = buf.readDouble();

        damageCfg = new FirstAidConfig.DamageSystem();
        damageCfg.maxHealthHead = buf.readByte();
        damageCfg.maxHealthLeftArm = buf.readByte();
        damageCfg.maxHealthLeftLeg = buf.readByte();
        damageCfg.maxHealthLeftFoot = buf.readByte();
        damageCfg.maxHealthBody = buf.readByte();
        damageCfg.maxHealthRightArm = buf.readByte();
        damageCfg.maxHealthRightLeg = buf.readByte();
        damageCfg.maxHealthRightFoot = buf.readByte();

        scaleMaxHealth = buf.readBoolean();
        capMaxHealth = buf.readBoolean();

        playerDamageModel = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(healingCfg.allowNaturalRegeneration);
        buf.writeBoolean(healingCfg.allowOtherHealingItems);
        buf.writeFloat(healingCfg.sleepHealing);
        buf.writeDouble(healingCfg.otherRegenMultiplier);
        buf.writeDouble(healingCfg.naturalRegenMultiplier);

        buf.writeByte(damageCfg.maxHealthHead);
        buf.writeByte(damageCfg.maxHealthLeftArm);
        buf.writeByte(damageCfg.maxHealthLeftLeg);
        buf.writeByte(damageCfg.maxHealthLeftFoot);
        buf.writeByte(damageCfg.maxHealthBody);
        buf.writeByte(damageCfg.maxHealthRightArm);
        buf.writeByte(damageCfg.maxHealthRightLeg);
        buf.writeByte(damageCfg.maxHealthRightFoot);

        buf.writeBoolean(scaleMaxHealth);
        buf.writeBoolean(capMaxHealth);

        ByteBufUtils.writeTag(buf, playerDamageModel);
    }

    public static class Handler implements IMessageHandler<MessageReceiveConfiguration, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageReceiveConfiguration message, MessageContext ctx) {
            FirstAid.activeHealingConfig = message.healingCfg;
            FirstAid.activeDamageConfig = message.damageCfg;
            FirstAid.scaleMaxHealth = message.scaleMaxHealth;
            FirstAid.capMaxHealth = message.capMaxHealth;
            AbstractPlayerDamageModel damageModel = PlayerDamageModel.create();
            damageModel.deserializeNBT(message.playerDamageModel);
            Minecraft mc = Minecraft.getMinecraft();

            FirstAid.logger.info("Received configuration");
            mc.addScheduledTask(() -> {
                PlayerDataManager.capList.put(mc.player, damageModel);
                if (damageModel.hasTutorial)
                    PlayerDataManager.tutorialDone.add(mc.player.getName());
                else
                    mc.player.sendMessage(new TextComponentString("[First Aid] " + I18n.format("firstaid.tutorial.hint", ClientProxy.showWounds.getDisplayName())));
            });
            return null;
        }
    }
}
