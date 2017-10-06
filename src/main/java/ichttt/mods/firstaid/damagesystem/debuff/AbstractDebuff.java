package ichttt.mods.firstaid.damagesystem.debuff;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;

public abstract class AbstractDebuff implements IDebuff {
    public final Potion effect;

    public AbstractDebuff(String potionName) {
        this.effect = Objects.requireNonNull(ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionName)));
    }
}
