package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomDamageDistribution extends DamageDistribution {
    public static final RandomDamageDistribution NEAREST_NOKILL = new RandomDamageDistribution(true, true);
    public static final RandomDamageDistribution NEAREST_KILL = new RandomDamageDistribution(true, false);
    public static final RandomDamageDistribution ANY_NOKILL = new RandomDamageDistribution(false, true);
    public static final RandomDamageDistribution ANY_KILL = new RandomDamageDistribution(false, false);

    private static final Random RANDOM = new Random();
    private final boolean nearestFirst;
    private final boolean tryNoKill;

    public RandomDamageDistribution(boolean nearestFirst, boolean tryNoKill) {
        this.nearestFirst = nearestFirst;
        this.tryNoKill = tryNoKill;
    }

    @Override
    protected float minHealth(@Nonnull EntityPlayer player, @Nonnull AbstractDamageablePart playerPart) {
        if (tryNoKill && playerPart.canCauseDeath)
            return 1F;
        return 0F;
    }

    @Override
    @Nonnull
    protected List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList() {
        if (nearestFirst) {
            int startValue = RANDOM.nextInt(4);
            return addAllRandom(startValue, RANDOM.nextBoolean());
        } else {
            List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();
            List<EntityEquipmentSlot> slots = Arrays.asList(EntityEquipmentSlot.values());
            Collections.shuffle(slots, RANDOM);
            for (EntityEquipmentSlot slot : slots) {
                if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND)
                    continue;
                List<EnumPlayerPart> parts = slotToParts.get(slot);
                Collections.shuffle(parts);
                partList.add(Pair.of(slot, parts.toArray(new EnumPlayerPart[0])));
            }
            return partList;
        }
    }

    public static List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> addAllRandom(int startValue, boolean up) {
        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();
        for (int i = 0; i < ARMOR_SLOTS.length; i ++) {
            int posInArray = Math.abs(i - (up ? 0 : 3)) + startValue;
            if (posInArray > 3)
                posInArray -= 4;
            EntityEquipmentSlot slot = ARMOR_SLOTS[posInArray];
            List<EnumPlayerPart> parts = slotToParts.get(slot);
            Collections.shuffle(parts);
            partList.add(Pair.of(slot, parts.toArray(new EnumPlayerPart[0])));
        }
        return partList;
    }
}
