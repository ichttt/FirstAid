package ichttt.mods.firstaid.api.registry;

import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class DebuffEntry extends IForgeRegistryEntry.Impl<DebuffEntry> {
    @Nonnull
    public final EnumDebuffSlot slot;
    @Nonnull
    public final Supplier<IDebuff> debuff;

    public DebuffEntry(@Nonnull EnumDebuffSlot slot, @Nonnull Supplier<IDebuff> debuff) {
        this.slot = slot;
        this.debuff = debuff;
    }
}
