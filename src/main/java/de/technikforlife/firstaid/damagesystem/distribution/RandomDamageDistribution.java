package de.technikforlife.firstaid.damagesystem.distribution;

import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomDamageDistribution extends DamageDistribution {
    protected static final Map<EntityEquipmentSlot, List<EnumPlayerPart>> slotToParts = new HashMap<>();
    protected static final EntityEquipmentSlot[] ARMOR_SLOTS;
    private static final Random RANDOM = new Random();
    private final boolean nearestFirst;

    public RandomDamageDistribution(boolean nearestFirst) { //TODO refactor into own class?
        this.nearestFirst = nearestFirst;
    }

    static {
        slotToParts.put(EntityEquipmentSlot.HEAD, Collections.singletonList(EnumPlayerPart.HEAD));
        slotToParts.put(EntityEquipmentSlot.CHEST, Arrays.asList(EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM, EnumPlayerPart.BODY));
        slotToParts.put(EntityEquipmentSlot.LEGS, Arrays.asList(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG));
        slotToParts.put(EntityEquipmentSlot.FEET, Arrays.asList(EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT));
        ARMOR_SLOTS = new EntityEquipmentSlot[4];
        ARMOR_SLOTS[3] = EntityEquipmentSlot.HEAD;
        ARMOR_SLOTS[2] = EntityEquipmentSlot.CHEST;
        ARMOR_SLOTS[1] = EntityEquipmentSlot.LEGS;
        ARMOR_SLOTS[0] = EntityEquipmentSlot.FEET;
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
