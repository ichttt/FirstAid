package de.technikforlife.firstaid.damagesystem.distribution;

import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomDamageDistribution extends DamageDistribution {
    private static final Map<EntityEquipmentSlot, List<EnumPlayerPart>> slotToParts = new HashMap<>();
    static {
        slotToParts.put(EntityEquipmentSlot.HEAD, Collections.singletonList(EnumPlayerPart.HEAD));
        slotToParts.put(EntityEquipmentSlot.CHEST, Arrays.asList(EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM, EnumPlayerPart.BODY));
        slotToParts.put(EntityEquipmentSlot.LEGS, Arrays.asList(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG));
        slotToParts.put(EntityEquipmentSlot.FEET, Arrays.asList(EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT));
    }

    @Override
    protected List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList() {
        List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();
        List<EntityEquipmentSlot> slots = Arrays.asList(EntityEquipmentSlot.values());
        Collections.shuffle(slots);
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
