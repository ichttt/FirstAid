package ichttt.mods.firstaid.api.registry;

import ichttt.mods.firstaid.api.IDamageDistribution;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public class DamageSourceEntry extends IForgeRegistryEntry.Impl<DamageSourceEntry> {
    @Nonnull
    public final String damageType;
    @Nonnull
    public final IDamageDistribution distributionTable;

    public DamageSourceEntry(@Nonnull String damageType, @Nonnull IDamageDistribution distributionTable) {
        this.damageType = damageType;
        this.distributionTable = distributionTable;
    }
}
