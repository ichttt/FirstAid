package ichttt.mods.firstaid.api.distribution;

import ichttt.mods.firstaid.api.IDamageDistribution;
import net.minecraft.util.DamageSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public abstract class DamageDistributionBuilderFactory {
    private static DamageDistributionBuilderFactory factory;

    /**
     * DO NOT USE! ONLY FOR INTERNAL USE
     */
    public static void setInstance(DamageDistributionBuilderFactory builderFactory) {
        factory = builderFactory;
    }

    /**
     * Use this to get the active instance.
     * Null if FirstAid is not active or a version without this feature (prior to 1.7.11) is loaded
     */
    @Nullable
    public static DamageDistributionBuilderFactory getInstance() {
        return factory;
    }

    /**
     * Creates a new {@link IStandardDamageDistributionBuilder}.
     * When {@link IDamageDistributionBuilder#registerStatic(DamageSource[])} or {@link IDamageDistributionBuilder#registerDynamic(Predicate)}
     * is called, the resulting distribution will be a StandardDamageDistribution.
     * @return A new builder
     */
    @Nonnull
    public abstract IStandardDamageDistributionBuilder newStandardBuilder();

    /**
     * Creates a new {@link IRandomDamageDistributionBuilder}.
     * When {@link IDamageDistributionBuilder#registerStatic(DamageSource[])} or {@link IDamageDistributionBuilder#registerDynamic(Predicate)}
     * is called, the resulting distribution will be a RandomDamageDistribution.
     * @return A new builder
     */
    @Nonnull
    public abstract IRandomDamageDistributionBuilder newRandomBuilder();

    /**
     * Creates a new {@link IEqualDamageDistributionBuilder}, meaning all incoming damage will be split on all limbs
     * When {@link IDamageDistributionBuilder#registerStatic(DamageSource[])} or {@link IDamageDistributionBuilder#registerDynamic(Predicate)}
     * is called, the resulting distribution will be a EqualDamageDistribution.
     * @return A new builder
     */
    @Nonnull
    public abstract IEqualDamageDistributionBuilder newEqualBuilder();

    /**
     * Creates a dummy builder for custom damage distribution implementation.
     * When {@link IDamageDistributionBuilder#registerStatic(DamageSource[])} or {@link IDamageDistributionBuilder#registerDynamic(Predicate)}
     * is called, the distribution will be registered.
     * @return A new builder
     */
    @Nonnull
    public abstract ICustomDamageDistributionBuilder newCustomBuilder(IDamageDistribution distribution);
}
