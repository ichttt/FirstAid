package ichttt.mods.firstaid.damagesystem.distribution;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
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
    protected List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList() {
        int posInArray = slot.getIndex();
        EntityEquipmentSlot slot = ARMOR_SLOTS[posInArray];
        List<EnumPlayerPart> parts = slotToParts.get(slot);
        Collections.shuffle(parts);
        return Collections.singletonList(Pair.of(slot, parts.toArray(new EnumPlayerPart[0])));
    }
}
