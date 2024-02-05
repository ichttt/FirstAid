package ichttt.mods.firstaid.common.damagesystem.debuff;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record OnHitDebuffEntry(float damageTakenThreshold, int effectDuration) {
    public static final Codec<OnHitDebuffEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("damageTakenThreshold").forGetter(o -> o.damageTakenThreshold),
                    ExtraCodecs.intRange(1, Short.MAX_VALUE).fieldOf("effectDuration").forGetter(o -> o.effectDuration)
            ).apply(instance, OnHitDebuffEntry::new)
    );
}