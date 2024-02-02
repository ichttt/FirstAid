package ichttt.mods.firstaid.api.distribution;

import com.mojang.serialization.Codec;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

public interface IDamageDistributionTarget {

    IDamageDistributionAlgorithm getAlgorithm();

    List<DamageType> buildApplyList(IForgeRegistry<DamageType> damageTypes);

    /**
     * Returns weather this target is dynamic of not.
     * Static takes precedence over dynamic, so if a static and dynamic target both specify an identical target, the static one will be used
     */
    boolean isDynamic();

    Codec<? extends IDamageDistributionTarget> codec();
}
