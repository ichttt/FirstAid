/*
 * FirstAid
 * Copyright (C) 2017-2022
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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.DirectDamageDistribution;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DebugDamageCommand {
    private static final SimpleCommandExceptionType TYPE = new SimpleCommandExceptionType(new TextComponent("0 is invalid as damage"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("damagePart").requires((source) -> source.hasPermission(2));
        List<EnumPlayerPart> allowedValues = new ArrayList<>(Arrays.asList(EnumPlayerPart.VALUES));
        allowedValues.add(null);

        for(EnumPlayerPart part : allowedValues) {
            builder.then(Commands.literal(part == null ? "ALL" : part.name())
                    .then(Commands.argument("damage", FloatArgumentType.floatArg())
                    .executes(context -> handleCommand(part, FloatArgumentType.getFloat(context, "damage"), true, context.getSource().getPlayerOrException()))
                    .then(Commands.literal("nodebuff")
                    .executes(context -> handleCommand(part, FloatArgumentType.getFloat(context, "damage"), false, context.getSource().getPlayerOrException())))));
        }
        dispatcher.register(builder);
    }

    private static int handleCommand(EnumPlayerPart part, float damage, boolean debuff, ServerPlayer player) throws CommandSyntaxException {
        if (damage == 0F)
            throw TYPE.create();
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        if (part == null) {
            for (EnumPlayerPart aPart : EnumPlayerPart.VALUES) {
                doDamage(aPart, damage, debuff, player, damageModel);
            }
        } else {
            doDamage(part, damage, debuff, player, damageModel);
        }

        FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> player), new MessageSyncDamageModel(damageModel, false));
        return 1;
    }

    private static void doDamage(EnumPlayerPart part, float damage, boolean debuff, ServerPlayer player, AbstractPlayerDamageModel damageModel) {
        if (damage > 0F) {
            DamageDistribution.handleDamageTaken(new DirectDamageDistribution(part, debuff), damageModel, damage, player, DamageSource.OUT_OF_WORLD, false, false);
        } else {
            damageModel.getFromEnum(part).heal(-damage, player, debuff);
        }
        if (damageModel.isDead(player)) {
            player.sendMessage(new TranslatableComponent("death.attack.generic", player.getDisplayName()), Util.NIL_UUID);
            CommonUtils.killPlayer(damageModel, player, null);
        }
    }
}
