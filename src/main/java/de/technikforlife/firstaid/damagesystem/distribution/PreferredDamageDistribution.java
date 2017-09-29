package de.technikforlife.firstaid.damagesystem.distribution;

import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class PreferredDamageDistribution extends DamageDistribution {
    private final EntityEquipmentSlot slot;

    public PreferredDamageDistribution(EnumPlayerPart preferred) {
        this.slot = preferred.slot;
        if (!RandomDamageDistribution.slotToParts.get(slot).contains(preferred)) //assertion
            throw new IllegalArgumentException("ArmorSlot " + slot + " is not for PlayerPart " + preferred);
    }

    @Nonnull
    @Override
    protected List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList() { //TODO refactor this to only return ONE list entry Pair<preferred, allOfPreferred>
        return Collections.singletonList(RandomDamageDistribution.addAllRandom(slot.getIndex(), true).get(0));
    }
}
