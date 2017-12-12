package ichttt.mods.firstaid.common.damagesystem.debuff;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public abstract class AbstractDebuff implements IDebuff {
    public final Potion effect;
    public final BooleanSupplier isEnabled;

    public AbstractDebuff(String potionName, BooleanSupplier isEnabled) {
        this.effect = Objects.requireNonNull(ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionName)));
        this.isEnabled = isEnabled;
    }
}
