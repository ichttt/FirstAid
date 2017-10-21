package ichttt.mods.firstaid.damagesystem.distribution;

import ichttt.mods.firstaid.damagesystem.DamageablePart;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.damagesystem.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HealthDistribution {
    private static final List<EnumPlayerPart> parts;
    static {
        EnumPlayerPart[] partArray = EnumPlayerPart.values();
        parts = new ArrayList<>(partArray.length);
        parts.addAll(Arrays.asList(partArray));
    }

    public static void distribute(float health, PlayerDamageModel damageModel, EntityPlayer player) {
        float toHeal = health / 8F;
        Collections.shuffle(parts);
        List<DamageablePart> damageableParts = new ArrayList<>(parts.size());
        for (EnumPlayerPart part : parts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }
        damageableParts.sort(Comparator.comparingDouble(value -> value.getMaxHealth() - value.currentHealth));

        for (int i = 0; i < 8; i++) {
            DamageablePart part = damageableParts.get(i);
            float diff = toHeal - part.heal(toHeal, player, !player.getEntityWorld().isRemote);
            //prevent inaccuracy
            diff = Math.round(diff * 10000.0F) / 10000.0F;

            health -= (toHeal - diff);
            if (i < 7)
                toHeal = health / (7F - i);
        }
    }

    public static void distributeHealth(float health, EntityPlayer player) {
        PlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
        distribute(health, damageModel, player);
    }
}
