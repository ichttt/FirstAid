package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.debuff.builder.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class DebuffBuilder implements IDebuffBuilder {
    @Nonnull
    public final String potionName;
    @Nonnull
    public final LinkedHashMap<Float, Integer> map = new LinkedHashMap<>();
    public final boolean isOnHit;
    @Nullable
    public SoundEvent sound;
    @Nullable
    public BooleanSupplier isEnabledSupplier;

    public DebuffBuilder(@Nonnull String potionName, boolean isOnHit) {
        this.potionName = Objects.requireNonNull(potionName);
        this.isOnHit = isOnHit;
    }

    @Override
    @Nonnull
    public DebuffBuilder addSoundEffect(@Nullable SoundEvent event) {
        this.sound = event;
        return this;
    }


    @Override
    @Nonnull
    public DebuffBuilder addBound(float value, int multiplier) {
        this.map.put(value, multiplier);
        return this;
    }

    @Override
    @Nonnull
    public DebuffBuilder addEnableCondition(@Nullable BooleanSupplier isEnabled) {
        this.isEnabledSupplier = isEnabled;
        return this;
    }

    @Override
    public void register(@Nonnull EnumDebuffSlot slot) {
        Objects.requireNonNull(FirstAidRegistry.getImpl()).registerDebuff(slot, this);
    }
}
