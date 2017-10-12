package ichttt.mods.firstaid.util;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.damagesystem.enums.EnumPlayerPart;
import ichttt.mods.firstaid.network.MessageReceiveDamage;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DebugDamageCommand extends CommandBase {

    @Nonnull
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (!(sender instanceof EntityPlayer))
            return Collections.emptyList();
        if (args.length == 1) {
            EnumPlayerPart[] parts = EnumPlayerPart.values();
            List<String> values = new ArrayList<>(parts.length);
            for (EnumPlayerPart part : parts)
                values.add(part.toString());
            return values;
        } else if (args.length == 3) {
            List<String> values = new ArrayList<>(2);
            values.add("true");
            values.add("false");
            return values;
        }
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public String getName() {
        return "damagePlayerPart";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        if (sender instanceof EntityPlayer)
            return "/damage [part] [damage] (apply debuff)";
        else
            return "Only usable by players";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer))
            throw new CommandException(getUsage(sender));
        if (args.length != 2 && args.length != 3)
            throw new CommandException("Missing arguments. Usage: " + getUsage(sender));
        try {
            EnumPlayerPart part = EnumPlayerPart.valueOf(args[0].toUpperCase());
            float damage = Float.parseFloat(args[1]);
            boolean debuff = true;
            if (args.length == 3)
                debuff = Boolean.parseBoolean(args[2]);
            EntityPlayer player = (EntityPlayer) sender;
            PlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
            damageModel.getFromEnum(part).damage(damage, player, debuff);
            FirstAid.NETWORKING.sendTo(new MessageReceiveDamage(part, damage), (EntityPlayerMP) player);
        } catch (RuntimeException e) {
            throw new CommandException(e.toString());
        }
    }
}
