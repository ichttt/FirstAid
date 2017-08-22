package de.technikforlife.firstaid;

import de.technikforlife.firstaid.damagesystem.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CommandGetHealth extends CommandBase {

    @Override
    public String getName() {
        return "getHealth";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        Entity entity = sender.getCommandSenderEntity();
        if (entity != null && entity.hasCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null)) {
            PlayerDamageModel damageModel = Objects.requireNonNull(entity.getCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null));
            sender.sendMessage(new TextComponentString("HEAD HEALTH: " + damageModel.HEAD.currentHealth));
            sender.sendMessage(new TextComponentString("LEFT ARM HEALTH: " + damageModel.LEFT_ARM.currentHealth));
            sender.sendMessage(new TextComponentString("LEFT LEG HEALTH: " + damageModel.LEFT_LEG.currentHealth));
            sender.sendMessage(new TextComponentString("BODY HEALTH: " + damageModel.BODY.currentHealth));
            sender.sendMessage(new TextComponentString("RIGHT ARM HEALTH: " + damageModel.RIGHT_ARM.currentHealth));
            sender.sendMessage(new TextComponentString("RIGHT LEG HEALTH: " + damageModel.RIGHT_LEG.currentHealth));
        } else {
            sender.sendMessage(new TextComponentString("Unable to obtain health :/"));
        }
    }
}
