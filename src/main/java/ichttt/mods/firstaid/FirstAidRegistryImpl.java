package ichttt.mods.firstaid;

import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.enums.EnumHealingType;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.damagesystem.distribution.DamageDistributions;
import ichttt.mods.firstaid.damagesystem.distribution.StandardDamageDistribution;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class FirstAidRegistryImpl extends FirstAidRegistry {
    public static final FirstAidRegistryImpl INSTANCE = new FirstAidRegistryImpl();
    private final Map<String, IDamageDistribution> DISTRIBUTION_MAP = new HashMap<>();
    private final Map<EnumHealingType, Function<EnumHealingType, AbstractPartHealer>> HEALER_MAP = new HashMap<>();

    public static void verify() {
        FirstAidRegistry registryImpl = FirstAidRegistry.getImpl();
        if (registryImpl == null)
            throw new IllegalStateException("The apiimpl has not been set! Something went seriously wrong!");
        if (registryImpl != INSTANCE)
            throw new IllegalStateException("A mod has registered a custom apiimpl for the registry. THIS IS NOT ALLOWED!" +
            "It should be " + INSTANCE.getClass().getName() + " but it actually is " + registryImpl.getClass().getName());
    }

    @Override
    public void bindDamageSourceStandard(@Nonnull String damageType, @Nonnull List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> priorityTable) {
        DISTRIBUTION_MAP.put(damageType, new StandardDamageDistribution(priorityTable));
    }

    @Override
    public void bindDamageSourceRandom(@Nonnull String damageType, boolean nearestFirst) {
        if (nearestFirst)
            DISTRIBUTION_MAP.remove(damageType);
        else
            DISTRIBUTION_MAP.put(damageType, DamageDistributions.FULL_RANDOM_DIST);
    }

    @Override
    public void bindDamageSourceCustom(@Nonnull String damageType, @Nonnull IDamageDistribution distributionTable) {
        DISTRIBUTION_MAP.put(damageType, distributionTable);
    }

    @Override
    public void bindHealingType(@Nonnull EnumHealingType type, @Nonnull Function<EnumHealingType, AbstractPartHealer> healer) {
        this.HEALER_MAP.put(type, healer);
    }

    @Nonnull
    @Override
    public AbstractPartHealer getPartHealer(@Nonnull EnumHealingType type) {
        return Objects.requireNonNull(HEALER_MAP.get(type), "Did not find part healer for healing type " + type).apply(type);
    }

    @Nonnull
    @Override
    public IDamageDistribution getDamageDistribution(@Nonnull DamageSource source) {
        IDamageDistribution distribution = DISTRIBUTION_MAP.get(source.damageType);
        if (distribution == null)
            distribution = DamageDistributions.SEMI_RANDOM_DIST;
        return distribution;
    }
}
