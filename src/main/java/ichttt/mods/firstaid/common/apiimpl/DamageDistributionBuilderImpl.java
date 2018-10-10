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
    @Override
    public IDamageDistribution createDamageDist(@Nonnull String damageType, @Nonnull List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> priorityTable) {
        return new StandardDamageDistribution(priorityTable);
    }

    @Override
    public IDamageDistribution createDamageDist(@Nonnull String damageType, boolean nearestFirst, boolean tryNoKill) {
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
