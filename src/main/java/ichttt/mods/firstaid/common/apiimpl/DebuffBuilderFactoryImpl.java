package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.api.debuff.builder.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.debuff.builder.IDebuffBuilder;

import javax.annotation.Nonnull;

public class DebuffBuilderFactoryImpl extends DebuffBuilderFactory {
    public static final DebuffBuilderFactoryImpl INSTANCE = new DebuffBuilderFactoryImpl();

    public static void verify() {
        DebuffBuilderFactory registryImpl = DebuffBuilderFactory.getInstance();
        if (registryImpl == null)
            throw new IllegalStateException("The apiimpl has not been set! Something went seriously wrong!");
        if (registryImpl != INSTANCE)
            throw new IllegalStateException("A mod has registered a custom apiimpl for the registry. THIS IS NOT ALLOWED!" +
                    "It should be " + INSTANCE.getClass().getName() + " but it actually is " + registryImpl.getClass().getName());
    }

    /**
     * Creates a new builder for a onHit debuff
     *
     * @param potionName The registry name of the potion
     * @return A new builder
     */
    @Override
    @Nonnull
    public IDebuffBuilder newOnHitDebuffBuilder(@Nonnull String potionName) {
        return new DebuffBuilder(potionName, true);
    }

    /**
     * Creates a new builder for a constant debuff
     *
     * @param potionName The registry name of the potion
     * @return A new builder
     */
    @Override
    @Nonnull
    public IDebuffBuilder newConstantDebuffBuilder(@Nonnull String potionName) {
        return new DebuffBuilder(potionName, false);
    }
}
