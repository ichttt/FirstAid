package ichttt.mods.firstaid.api;

import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.enums.EnumHealingType;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * The central registry for FirstAid.
 * Impl is set in PreInit, default values at init
 * If you want to register your own values, you should probably do it in init
 * If you want to override the default values, you should probably do it in PostInit
 */
public abstract class FirstAidRegistry {
    @Nullable
    private static FirstAidRegistry instance;

    /**
     * DO NOT USE! ONLY FOR INTERNAL THINGS
     */
    public static void setImpl(@Nonnull FirstAidRegistry registry) {
        instance = registry;
    }

    /**
     * Use this to get the active instance.
     * Null if FirstAid is not active or a version without this feature (prior to 1.3.2) is loaded
     */
    @Nullable
    public static FirstAidRegistry getImpl() {
        return instance;
    }

    /**
     * Binds the damage source to a distribution.
     * The distribution will be a StandardDamageDistribution
     * @param damageType The source
     * @param priorityTable The distribution table. The first item on the list will be damaged first,
     *                     if the health there drops under zero, the second will be damaged and so on
     */
    public abstract void bindDamageSourceStandard(@Nonnull String damageType, @Nonnull List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> priorityTable);

    /**
     * @deprecated Use {@link #bindDamageSourceRandom(String, boolean, boolean)}
     */
    @Deprecated
    public abstract void bindDamageSourceRandom(@Nonnull String damageType, boolean nearestFirst);

    /**
     * Binds the damage source to a distribution.
     * The distribution will be a RandomDamageDistribution
     * This (with nearestFirst = true) is the default setting when nothing else is specified
     * @param damageType The source
     * @param nearestFirst True, if only a random start point should be chosen and the nearest other parts will be damaged
     *                    if the health there drops under zero, false if everything should be random
     * @param tryNoKill If true, head and torso will only drop to 1 health and will only die if there is nothing else left
     */
    public abstract void bindDamageSourceRandom(@Nonnull String damageType, boolean nearestFirst, boolean tryNoKill);

    /**
     * Binds the damage source to a custom distribution
     * @param damageType The source
     * @param distributionTable Your custom distribution
     */
    public abstract void bindDamageSourceCustom(@Nonnull String damageType, @Nonnull IDamageDistribution distributionTable);

    public abstract void bindHealingType(@Nonnull EnumHealingType type, @Nonnull Function<EnumHealingType, AbstractPartHealer> factory);

    @Nonnull
    public abstract AbstractPartHealer getPartHealer(@Nonnull EnumHealingType type);

    @Nonnull
    public abstract IDamageDistribution getDamageDistribution(@Nonnull DamageSource source);
}
