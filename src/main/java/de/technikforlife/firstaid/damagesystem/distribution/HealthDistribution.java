package de.technikforlife.firstaid.damagesystem.distribution;

import de.technikforlife.firstaid.damagesystem.DamageablePart;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.PlayerDataManager;
import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
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

    private static float distribute(float health, PlayerDamageModel damageModel, EntityPlayer player) {
        float toHeal = health / 8F;
        Collections.shuffle(parts);
        List<DamageablePart> damageableParts = new ArrayList<>(parts.size());
        for (EnumPlayerPart part : parts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }
        damageableParts.sort(Comparator.comparingDouble(value -> value.maxHealth - value.currentHealth));

        for (int i = 0; i < 8; i++) {
            DamageablePart part = damageableParts.get(i);
            float diff = toHeal - part.heal(toHeal, player, !player.getEntityWorld().isRemote);
            //prevent inaccuracy
            diff = Math.round(diff * 10000.0F) / 10000.0F;

            health -= (toHeal - diff);
            if (i < 7)
                toHeal = health / (7F - i);
        }
        health = Math.round(health * 10000.0F) / 10000.0F;
        return health;
    }

    //TODO optimise this
    public static void distributeHealth(float health, EntityPlayer player) {
        PlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
        float rest = distribute(health, damageModel, player);
        if (rest > 0) //try a second time. Not optimal. Should really try to find a better way
            distribute(rest, damageModel, player);
    }
}
