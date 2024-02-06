package ichttt.mods.firstaid.common.registries;

import com.mojang.serialization.Codec;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionTarget;
import ichttt.mods.firstaid.common.apiimpl.StaticDamageDistributionTarget;
import ichttt.mods.firstaid.common.apiimpl.TagDamageDistributionTarget;
import ichttt.mods.firstaid.common.damagesystem.debuff.builder.ConstantDebuffBuilder;
import ichttt.mods.firstaid.common.damagesystem.debuff.builder.OnHitDebuffBuilder;
import ichttt.mods.firstaid.common.damagesystem.distribution.DirectDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.damagesystem.distribution.EqualDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistributionAlgorithm;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class FirstAidRegistries {

    public static class Keys {
        static final ResourceKey<Registry<Codec<? extends IDamageDistributionAlgorithm>>> DAMAGE_DISTRIBUTION_ALGORITHMS = key("damage_distribution_algorithms");
        static final ResourceKey<Registry<Codec<? extends IDamageDistributionTarget>>> DAMAGE_DISTRIBUTION_TARGETS = key("damage_distribution_targets");
        static final ResourceKey<Registry<IDamageDistributionTarget>> DAMAGE_DISTRIBUTIONS = key("damage_distributions");

        static final ResourceKey<Registry<Codec<? extends IDebuffBuilder>>> DEBUFF_BUILDERS = key("debuff_builders");
        static final ResourceKey<Registry<IDebuffBuilder>> DEBUFFS = key("debuffs");


        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(new ResourceLocation(FirstAid.MODID, name));
        }
    }
    // --- REGULAR REGISTRIES ---
    static final DeferredRegister<Codec<? extends IDamageDistributionAlgorithm>> DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS = DeferredRegister.create(Keys.DAMAGE_DISTRIBUTION_ALGORITHMS, FirstAid.MODID);
    public static final Supplier<IForgeRegistry<Codec<? extends IDamageDistributionAlgorithm>>> DAMAGE_DISTRIBUTION_ALGORITHMS = DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.makeRegistry(() -> new RegistryBuilder<Codec<? extends IDamageDistributionAlgorithm>>().disableSaving());

    static final DeferredRegister<Codec<? extends IDamageDistributionTarget>> DEFERRED_DAMAGE_DISTRIBUTION_TARGETS = DeferredRegister.create(Keys.DAMAGE_DISTRIBUTION_TARGETS, FirstAid.MODID);
    public static final Supplier<IForgeRegistry<Codec<? extends IDamageDistributionTarget>>> DAMAGE_DISTRIBUTION_TARGETS = DEFERRED_DAMAGE_DISTRIBUTION_TARGETS.makeRegistry(() -> new RegistryBuilder<Codec<? extends IDamageDistributionTarget>>().disableSaving().disableOverrides());

    static final DeferredRegister<Codec<? extends IDebuffBuilder>> DEFERRED_DEBUFF_BUILDERS = DeferredRegister.create(Keys.DEBUFF_BUILDERS, FirstAid.MODID);
    public static final Supplier<IForgeRegistry<Codec<? extends IDebuffBuilder>>> DEBUFF_BUILDERS = DEFERRED_DEBUFF_BUILDERS.makeRegistry(() -> new RegistryBuilder<Codec<? extends IDebuffBuilder>>().disableSaving().disableOverrides());

    static {
        DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.register("direct", () -> DirectDamageDistributionAlgorithm.CODEC);
        DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.register("equal", () -> EqualDamageDistributionAlgorithm.CODEC);
        DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.register("random", () -> RandomDamageDistributionAlgorithm.CODEC);
        DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.register("standard", () -> StandardDamageDistributionAlgorithm.CODEC);

        DEFERRED_DAMAGE_DISTRIBUTION_TARGETS.register("static", () -> StaticDamageDistributionTarget.CODEC);
        DEFERRED_DAMAGE_DISTRIBUTION_TARGETS.register("tag", () -> TagDamageDistributionTarget.CODEC);

        DEFERRED_DEBUFF_BUILDERS.register("constant", () -> ConstantDebuffBuilder.CODEC);
        DEFERRED_DEBUFF_BUILDERS.register("on_hit", () -> OnHitDebuffBuilder.CODEC);
    }

    // --- DATA DRIVEN REGISTRIES
    public static void createDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(Keys.DAMAGE_DISTRIBUTIONS, IDamageDistributionTarget.DIRECT_CODEC, IDamageDistributionTarget.DIRECT_CODEC);
        event.dataPackRegistry(Keys.DEBUFFS, IDebuffBuilder.DIRECT_CODEC, IDebuffBuilder.DIRECT_CODEC);
    }

    public static void setup(IEventBus modEventBus) {
        DEFERRED_DAMAGE_DISTRIBUTION_ALGORITHMS.register(modEventBus);
        DEFERRED_DAMAGE_DISTRIBUTION_TARGETS.register(modEventBus);
        DEFERRED_DEBUFF_BUILDERS.register(modEventBus);
        modEventBus.addListener(FirstAidRegistries::createDataPackRegistries);
    }
}
