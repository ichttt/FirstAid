/*
 * FirstAid
 * Copyright (C) 2017-2020
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.DirectDamageDistribution;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
            values.add("ALL");
            return getListOfStringsMatchingLastWord(args, values);
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
            return "/damage [part] [damage] (invoke debuffs)";
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
            float damage = Float.parseFloat(args[1]);
            boolean debuff = true;
            if (args.length == 3)
                debuff = Boolean.parseBoolean(args[2]);

            if (args[0].equalsIgnoreCase("ALL")) {
                for (EnumPlayerPart part : EnumPlayerPart.VALUES)
                    damage(part, damage, debuff, (EntityPlayer) sender);
            } else {
                EnumPlayerPart part = EnumPlayerPart.valueOf(args[0].toUpperCase(Locale.ENGLISH));
                damage(part, damage, debuff, (EntityPlayer) sender);
            }
            if (sender instanceof EntityPlayerMP && !(sender instanceof FakePlayer))
                FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(Objects.requireNonNull(((EntityPlayer) sender).getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)), false), (EntityPlayerMP) sender);
        } catch (RuntimeException e) {
            throw new CommandException(e.toString());
        }
    }

    private static void damage(EnumPlayerPart part, float damage, boolean debuff, EntityPlayer player) {
        if (damage == 0F)
            return;
        AbstractPlayerDamageModel damageModel = Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
        if (damage > 0F) {
            DamageDistribution.handleDamageTaken(new DirectDamageDistribution(part, debuff), damageModel, damage, player, DamageSource.OUT_OF_WORLD, false, false);
        } else {
            damageModel.getFromEnum(part).heal(-damage, player, debuff);
        }
    }
}
