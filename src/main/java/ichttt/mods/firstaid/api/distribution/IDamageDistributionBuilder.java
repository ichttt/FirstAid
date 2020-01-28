package ichttt.mods.firstaid.api.distribution;

import net.minecraft.util.DamageSource;

import java.util.function.Predicate;

public interface IDamageDistributionBuilder {

    /**
     * Binds a matcher predicate.
     * The matcher should be simple, as it will be called every time no static fitting distribution could be found.
     * Use {@link #registerStatic(DamageSource[])} whenever possible, as it is the faster option
     * @param matcher The matcher to select whether this distribution should be used or not.
     *                Will only be called if no static distribution could be found
     */
    void registerDynamic(Predicate<DamageSource> matcher);

    /**
     * Binds the damage source to a distribution.
     * This should be preferred over {@link #registerDynamic(Predicate)} whenever possible
     * @param source The sources that should use this distribution
     */
    void registerStatic(DamageSource... source);
}
