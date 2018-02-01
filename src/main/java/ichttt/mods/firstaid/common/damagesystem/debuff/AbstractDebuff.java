package ichttt.mods.firstaid.common.damagesystem.debuff;

import ichttt.mods.firstaid.api.debuff.IDebuff;
import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public abstract class AbstractDebuff implements IDebuff {
    @Nonnull
    public final Potion effect;
    @Nonnull
    public final BooleanSupplier isEnabled;
    @Nonnull
    protected final Float2IntLinkedOpenHashMap map;

    public AbstractDebuff(@Nonnull String potionName, @Nonnull Float2IntLinkedOpenHashMap map, @Nonnull BooleanSupplier isEnabled) {
        this.effect = Objects.requireNonNull(ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionName)));
        this.isEnabled = isEnabled;
        this.map = map;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled.getAsBoolean();
    }
}
