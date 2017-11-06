package ichttt.mods.firstaid.damagesystem.distribution;

import ichttt.mods.firstaid.api.AbstractDamageablePart;
import ichttt.mods.firstaid.api.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;

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

    public static void distribute(float health, AbstractPlayerDamageModel damageModel, EntityPlayer player) {
        float toHeal = health / 8F;
        Collections.shuffle(parts);
        List<AbstractDamageablePart> damageableParts = new ArrayList<>(parts.size());
        for (EnumPlayerPart part : parts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }
        damageableParts.sort(Comparator.comparingDouble(value -> value.getMaxHealth() - value.currentHealth));

        for (int i = 0; i < 8; i++) {
            AbstractDamageablePart part = damageableParts.get(i);
            float diff = toHeal - part.heal(toHeal, player, !player.getEntityWorld().isRemote);
            //prevent inaccuracy
            diff = Math.round(diff * 10000.0F) / 10000.0F;

            health -= (toHeal - diff);
            if (i < 7)
                toHeal = health / (7F - i);
        }
    }

    public static void distributeHealth(float health, EntityPlayer player) {
        AbstractPlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
        distribute(health, damageModel, player);
    }
}
