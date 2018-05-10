package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
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

    public static void manageHealth(float health, AbstractPlayerDamageModel damageModel, EntityPlayer player, boolean sendChanges, boolean distribute) {
        if (sendChanges && player.world.isRemote) {
            FirstAid.LOGGER.catching(new RuntimeException("Someone set flag sendChanges on the client, this is not supported!"));
            sendChanges = false;
        } else if (sendChanges && !(player instanceof EntityPlayerMP)) { //EntityOtherPlayerMP? log something?
            sendChanges = false;
        }

        float toHeal = distribute ? health / 8F : health;
        Collections.shuffle(parts, player.world.rand);
        List<AbstractDamageablePart> damageableParts = new ArrayList<>(parts.size());

        for (EnumPlayerPart part : parts) {
            damageableParts.add(damageModel.getFromEnum(part));
        }

        if (distribute)
            damageableParts.sort(Comparator.comparingDouble(value -> value.getMaxHealth() - value.currentHealth));
        float[] healingDone = new float[8];

        for (int i = 0; i < 8; i++) {
            AbstractDamageablePart part = damageableParts.get(i);
            float diff = toHeal - part.heal(toHeal, player, !player.world.isRemote);
            //prevent inaccuracy
            diff = Math.round(diff * 10000.0F) / 10000.0F;
            healingDone[part.part.id - 1] = diff;

            health -= diff;
            if (distribute) {
                if (i < 7)
                    toHeal = health / (7F - i);
            } else {
                System.out.println(String.format("Healed %s for %s", part.part, diff));
                toHeal -= diff;
                if (toHeal <= 0)
                    break;
            }
        }

        if (sendChanges)
            FirstAid.NETWORKING.sendTo(new MessageAddHealth(healingDone), (EntityPlayerMP) player);
    }

    public static void distributeHealth(float health, EntityPlayer player, boolean sendChanges) {
        AbstractPlayerDamageModel damageModel = player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
        manageHealth(health, damageModel, player, sendChanges, true);
    }

    public static void addRandomHealth(float health, EntityPlayer player, boolean sendChanges) {
        AbstractPlayerDamageModel damageModel = player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null);
        manageHealth(health, damageModel, player, sendChanges, false);
    }
}
