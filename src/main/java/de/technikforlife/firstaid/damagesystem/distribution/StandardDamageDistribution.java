package de.technikforlife.firstaid.damagesystem.distribution;

import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class StandardDamageDistribution extends DamageDistribution {
    private final List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();

    public StandardDamageDistribution addParts(EntityEquipmentSlot slot, EnumPlayerPart... part) {
        partList.add(Pair.of(slot, part));
        return this;
    }

    @Override
    protected List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList() {
        return partList;
    }
}
