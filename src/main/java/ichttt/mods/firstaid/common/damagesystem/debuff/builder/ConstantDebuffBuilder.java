package ichttt.mods.firstaid.common.damagesystem.debuff.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.debuff.IDebuffBuilder;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.common.damagesystem.debuff.ConstantDebuff;
import ichttt.mods.firstaid.common.damagesystem.debuff.ConstantDebuffEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.List;

public class ConstantDebuffBuilder implements IDebuffBuilder {
    public static final Codec<ConstantDebuffBuilder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    StringRepresentable.fromEnum(EnumDebuffSlot::values).fieldOf("debuffSlot").forGetter(o -> o.debuffSlot),
                    ResourceLocation.CODEC.fieldOf("potionName").forGetter(o -> o.effect),
                    ConstantDebuffEntry.CODEC.listOf().fieldOf("amplifierBoundaries").forGetter(o -> o.amplifierBoundaries)
            ).apply(instance, ConstantDebuffBuilder::new)
    );
    private final EnumDebuffSlot debuffSlot;
    private final ResourceLocation effect;
    private final List<ConstantDebuffEntry> amplifierBoundaries;

    public ConstantDebuffBuilder(EnumDebuffSlot debuffSlot, ResourceLocation effect, List<ConstantDebuffEntry> amplifierBoundaries) {
        this.debuffSlot = debuffSlot;
        this.effect = effect;
        this.amplifierBoundaries = amplifierBoundaries;
    }

    @Override
    public Codec<? extends IDebuffBuilder> codec() {
        return CODEC;
    }

    @Override
    public EnumDebuffSlot affectedSlot() {
        return this.debuffSlot;
    }

    @Override
    public IDebuff build() {
        return new ConstantDebuff(this.effect, this.amplifierBoundaries);
    }
}
