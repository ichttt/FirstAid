package ichttt.mods.firstaid.api.registry;

import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class DamageDistributionBuilder {
    @Nullable
    private static DamageDistributionBuilder instance;

    /**
     * DO NOT USE! ONLY FOR INTERNAL THINGS
     */
    public static void setImpl(@Nonnull DamageDistributionBuilder registry) {
        instance = registry;
    }

    /**
     * Use this to get the active instance.
     * Null if FirstAid is not active or a version without this feature (prior to 1.5.10) is loaded
     */
    @Nullable
    public static DamageDistributionBuilder getImpl() {
        return instance;
    }

    /**
     * Gives you a {@link IDamageDistribution} for registering during {@code RegistryEvent.Register<DamageSourceEntry>}
     * The distribution will be a StandardDamageDistribution
     *
     * @param damageType    The source
     * @param priorityTable The distribution table. The first item on the list will be damaged first,
     *                      if the health there drops under zero, the second will be damaged and so on
     */
    public abstract IDamageDistribution createDamageDist(@Nonnull String damageType, @Nonnull List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> priorityTable);

    /**
     * Gives you a {@link IDamageDistribution} for registering during {@code RegistryEvent.Register<DamageSourceEntry>}
     * The distribution will be a RandomDamageDistribution
     * This (with nearestFirst = true) is the default setting when nothing else is specified
     *
     * @param damageType   The source
     * @param nearestFirst True, if only a random start point should be chosen and the nearest other parts will be damaged
     *                     if the health there drops under zero, false if everything should be random
     * @param tryNoKill    If true, head and torso will only drop to 1 health and will only die if there is nothing else left
     */
    public abstract IDamageDistribution createDamageDist(@Nonnull String damageType, boolean nearestFirst, boolean tryNoKill);
}
