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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

public class DebugDamageCommand {
    private static final SimpleCommandExceptionType TYPE = new SimpleCommandExceptionType(new StringTextComponent("0 is invalid as damage"));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("damagePart").requires((source) -> source.hasPermission(2));

        for(EnumPlayerPart part : EnumPlayerPart.VALUES) {
            builder.then(Commands.literal(part.name())
                    .then(Commands.argument("damage", FloatArgumentType.floatArg())
                    .executes(context -> damage(part, FloatArgumentType.getFloat(context, "damage"), true, context.getSource().getPlayerOrException()))
                    .then(Commands.literal("nodebuff")
                    .executes(context -> damage(part, FloatArgumentType.getFloat(context, "damage"), false, context.getSource().getPlayerOrException())))));
        }
        dispatcher.register(builder);
    }

    private static int damage(EnumPlayerPart part, float damage, boolean debuff, ServerPlayerEntity player) throws CommandSyntaxException {
        if (damage == 0F)
            throw TYPE.create();
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        if (damage > 0F) {
            DamageDistribution.handleDamageTaken(new DirectDamageDistribution(part, debuff), damageModel, damage, player, DamageSource.OUT_OF_WORLD, false, false);
        } else {
            damageModel.getFromEnum(part).heal(-damage, player, debuff);
        }
        if (damageModel.isDead(player)) {
            player.sendMessage(new TranslationTextComponent("death.attack.generic", player.getDisplayName()), Util.NIL_UUID);
            CommonUtils.killPlayer(damageModel, player, null);
        }
        FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> player), new MessageSyncDamageModel(damageModel, false));
        return 1;
    }
}
