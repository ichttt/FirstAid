package ichttt.mods.firstaid.api.debuff.builder;

import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

/**
 * Use this if you want to add simple onHit or constant debuffs.
 * <br>
 * If you want to do your own, custom implementation, you can use {@link ichttt.mods.firstaid.api.debuff.IDebuff}
 * directly and register it using {@link ichttt.mods.firstaid.api.FirstAidRegistry#registerDebuff(EnumDebuffSlot, IDebuff)}.
 */
public interface IDebuffBuilder {

    /**
     * Adds a sound to the debuff. The sound will be played as long as the debuff is timed.
     * <b>Does only work with onHit debuffs!</b>
     *
     * @param event The sound that should be player
     * @return this
     */
    @Nonnull
    IDebuffBuilder addSoundEffect(@Nullable SoundEvent event);

    /**
     * If OnHit damage: value = absolute damage taken for this multiplier to apply;
     * If Constant: value = percentage of health left for this multiplier
     *
     * @param value      absolute damage (onHit) or percentage of the health left
     * @param multiplier the potion effect multiplier
     * @return this
     */
    @Nonnull
    IDebuffBuilder addBound(float value, int multiplier);

    /**
     * Provide a boolean supplier to control runtime-disabling of certain effects (e.g. via config)
     *
     * @param isEnabled A supplier that should return true if the debug should be applied
     * @return this
     */
    @Nonnull
    IDebuffBuilder addEnableCondition(@Nullable BooleanSupplier isEnabled);

    /**
     * Builds and registers this debuff to the FirstAid registry.
     * This is the final step.
     *
     * @param slot The slot where the debuff should apply
     */
    void register(@Nonnull EnumDebuffSlot slot);
}
