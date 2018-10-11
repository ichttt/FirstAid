package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.api.registry.DamageDistributionBuilder;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistribution;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

public class DamageDistributionBuilderImpl extends DamageDistributionBuilder {
    private static final DamageDistributionBuilderImpl INSTANCE = new DamageDistributionBuilderImpl();

    static void init() {
        DamageDistributionBuilder.setImpl(INSTANCE);
    }

    static void verify() {
        DamageDistributionBuilder registryImpl = DamageDistributionBuilder.getImpl();
        if (registryImpl == null)
            throw new IllegalStateException("The apiimpl has not been set! Something went seriously wrong!");
        if (registryImpl != INSTANCE)
            throw new IllegalStateException("A mod has registered a custom apiimpl for the registry. THIS IS NOT ALLOWED!" +
                    "It should be " + INSTANCE.getClass().getName() + " but it actually is " + registryImpl.getClass().getName());
    }

    @Override
    public IDamageDistribution standardDist(@Nonnull List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> priorityTable) {
        return new StandardDamageDistribution(priorityTable);
    }

    @Override
    public IDamageDistribution randomDist(boolean nearestFirst, boolean tryNoKill) {
        if (nearestFirst) {
            if (!tryNoKill)
                return RandomDamageDistribution.NEAREST_KILL;
            else
                return RandomDamageDistribution.NEAREST_NOKILL;
        } else {
            return tryNoKill ? RandomDamageDistribution.ANY_NOKILL : RandomDamageDistribution.ANY_KILL;
        }
    }
}
