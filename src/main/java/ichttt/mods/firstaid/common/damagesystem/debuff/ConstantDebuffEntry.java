package ichttt.mods.firstaid.common.damagesystem.debuff;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record ConstantDebuffEntry(float healthFractionThreshold, int effectAmplifier) {
    public static final Codec<ConstantDebuffEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("healthFractionThreshold").forGetter(o -> o.healthFractionThreshold),
                    ExtraCodecs.intRange(0, Byte.MAX_VALUE).fieldOf("effectAmplifier").forGetter(o -> o.effectAmplifier)
            ).apply(instance, ConstantDebuffEntry::new)
    );
}
