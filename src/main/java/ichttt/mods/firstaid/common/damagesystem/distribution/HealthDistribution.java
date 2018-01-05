package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.common.network.MessageAddHealth;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HealthDistribution {
    private static final List<EnumPlayerPart> parts;
    static {
        EnumPlayerPart[] partArray = EnumPlayerPart.VALUES;
        parts = new ArrayList<>(partArray.length);
        parts.addAll(Arrays.asList(partArray));
    }

    public static void distribute(float health, AbstractPlayerDamageModel damageModel, EntityPlayer player, boolean sendChanges) {
        if (sendChanges && player.world.isRemote) {
            FirstAid.logger.catching(new RuntimeException("Someone set flag sendChanges on the client, this is not supported!"));
            sendChanges = false;
        } else if (sendChanges && !(player instanceof EntityPlayerMP)) { //EntityOtherPlayerMP? log something?
            sendChanges = false;
        }

        float toHeal = health / 8F;
        Collections.shuffle(parts);
        List<AbstractDamageablePart> damageableParts = new ArrayList<>(parts.size());
        for (EnumPlayerPart part : parts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }
        damageableParts.sort(Comparator.comparingDouble(value -> value.getMaxHealth() - value.currentHealth));
        float[] healingDone = new float[8];

        for (int i = 0; i < 8; i++) {
            AbstractDamageablePart part = damageableParts.get(i);
            float diff = toHeal - part.heal(toHeal, player, !player.world.isRemote);
            //prevent inaccuracy
            diff = Math.round(diff * 10000.0F) / 10000.0F;
            healingDone[part.part.id - 1] = diff;

            health -= diff;
            if (i < 7)
                toHeal = health / (7F - i);
        }

        if (sendChanges) FirstAid.NETWORKING.sendTo(new MessageAddHealth(healingDone), (EntityPlayerMP) player);
    }

    public static void distributeHealth(float health, EntityPlayer player, boolean sendChanges) {
        AbstractPlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
        distribute(health, damageModel, player, sendChanges);
    }
}
