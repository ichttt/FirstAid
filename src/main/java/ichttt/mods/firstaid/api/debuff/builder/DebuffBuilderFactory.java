package ichttt.mods.firstaid.api.debuff.builder;

import javax.annotation.Nonnull;

/**
 * This is the class for the default builders. Use it whenever possible
 */
public abstract class DebuffBuilderFactory {
    private static DebuffBuilderFactory instance;

    /**
     * DO NOT USE! ONLY FOR INTERNAL THINGS
     */
    public static void setInstance(DebuffBuilderFactory factory) {
        instance = factory;
    }

    /**
     * Use this to get the active instance.
     * Null if FirstAid is not active or a version without this feature (prior to 1.4.2) is loaded
     */
    public static DebuffBuilderFactory getInstance() {
        return instance;
    }

    /**
     * Creates a new builder for a onHit debuff
     *
     * @param potionName The registry name of the potion
     * @return A new builder
     */
    @Nonnull
    public abstract IDebuffBuilder newOnHitDebuffBuilder(@Nonnull String potionName);

    /**
     * Creates a new builder for a constant debuff
     *
     * @param potionName The registry name of the potion
     * @return A new builder
     */
    @Nonnull
    public abstract IDebuffBuilder newConstantDebuffBuilder(@Nonnull String potionName);
}
