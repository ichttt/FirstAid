package ichttt.mods.firstaid.common.apiimpl.distribution;

import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.distribution.IStandardDamageDistributionBuilder;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistribution;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class StandardDamageDistributionBuilder extends BaseDamageDistributionBuilder implements IStandardDamageDistributionBuilder {
    private final ArrayList<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList = new ArrayList<>();
    private boolean ignoreOrder;

    @Nonnull
    @Override
    public IStandardDamageDistributionBuilder addDistributionLayer(EntityEquipmentSlot slot, EnumPlayerPart... parts) {
        partList.add(Pair.of(slot, parts));
        return this;
    }

    @Nonnull
    @Override
    public IStandardDamageDistributionBuilder ignoreOrder() {
        this.ignoreOrder = true;
        return this;
    }

    public IDamageDistribution build() {
        partList.trimToSize();
        if (partList.size() == 0) throw new IllegalArgumentException("Missing parts!");
        return new StandardDamageDistribution(partList, ignoreOrder);
    }
}
