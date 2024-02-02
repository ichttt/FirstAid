package ichttt.mods.firstaid.common.registries;

import com.mojang.serialization.Codec;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionTarget;
import ichttt.mods.firstaid.common.damagesystem.distribution.DirectDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.damagesystem.distribution.EqualDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistributionAlgorithm;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class FirstAidRegistries {

    public static class Keys {
        private static final ResourceKey<Registry<Codec<? extends IDamageDistributionAlgorithm>>> DAMAGE_DISTRIBUTION_ALGORITHMS = key("damage_distribution_algorithms");
        private static final ResourceKey<Registry<Codec<? extends IDamageDistributionTarget>>> DAMAGE_DISTRIBUTION_TARGETS = key("damage_distribution_targets"); //TODO target types regular, targets data driven


        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(new ResourceLocation(FirstAid.MODID, name));
        }
    }
    // --- REGULAR REGISTRIES ---
    static final DeferredRegister<Codec<? extends IDamageDistributionAlgorithm>> DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS = DeferredRegister.create(Keys.DAMAGE_DISTRIBUTION_ALGORITHMS, FirstAid.MODID);
    public static final Supplier<IForgeRegistry<Codec<? extends IDamageDistributionAlgorithm>>> DAMAGE_DISTRIBUTION_ALGORITHMS = DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.makeRegistry(() -> new RegistryBuilder<Codec<? extends IDamageDistributionAlgorithm>>().disableSync().disableSaving());

    static final DeferredRegister<Codec<? extends IDamageDistributionTarget>> DEFERRED_DAMAGE_DISTRIBUTION_TARGETS = DeferredRegister.create(Keys.DAMAGE_DISTRIBUTION_TARGETS, FirstAid.MODID);
    public static final Supplier<IForgeRegistry<Codec<? extends IDamageDistributionTarget>>> DAMAGE_DISTRIBUTION_TARGETS = DEFERRED_DAMAGE_DISTRIBUTION_TARGETS.makeRegistry(() -> new RegistryBuilder<Codec<? extends IDamageDistributionTarget>>().disableSaving().disableSync().disableOverrides());


    static {
        // TODO move
        DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.register("direct", () -> DirectDamageDistributionAlgorithm.CODEC);
        DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.register("equal", () -> EqualDamageDistributionAlgorithm.CODEC);
        DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.register("random", () -> RandomDamageDistributionAlgorithm.CODEC);
        DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.register("standard", () -> StandardDamageDistributionAlgorithm.CODEC);
    }

    public static void setup(IEventBus bus) {
        DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.register(bus);
        DEFERRED_DAMAGE_DISTRIBUTION_TARGETS.register(bus);
    }
}
