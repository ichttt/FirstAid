package ichttt.mods.firstaid.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.network.MessageReceiveDamage;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.network.PacketDistributor;

public class DebugDamageCommand {
    private static final SimpleCommandExceptionType TYPE = new SimpleCommandExceptionType(new TextComponentString("0 is invalid as damage"));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("damagePart").requires((source) -> source.hasPermissionLevel(2));

        for(EnumPlayerPart part : EnumPlayerPart.VALUES) {
            builder.then(Commands.literal(part.name())
                   .then(Commands.argument("damage", FloatArgumentType.floatArg())
                   .executes(context -> damage(part, FloatArgumentType.getFloat(context, "damage"), true, context.getSource().asPlayer()))));
        }
        dispatcher.register(builder);
    }

    private static int damage(EnumPlayerPart part, float damage, boolean debuff, EntityPlayerMP player) throws CommandSyntaxException {
        if (damage == 0F)
            throw TYPE.create();
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        if (damage > 0F) {
            damageModel.getFromEnum(part).damage(damage, player, debuff);
        } else {
            damageModel.getFromEnum(part).heal(-damage, player, debuff);
        }
        FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> player), new MessageReceiveDamage(part, damage, 0F));
        if (damageModel.isDead(player)) {
            player.sendMessage(new TextComponentTranslation("death.attack.generic", player.getDisplayName()));
            CommonUtils.killPlayer(player, null);
        }
        return 1;
    }
}
