package ichttt.mods.firstaid.api.registry;

import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class HealingEntry extends IForgeRegistryEntry.Impl<HealingEntry> {
    @Nonnull
    public final Item item;
    @Nonnull
    public final Function<ItemStack, AbstractPartHealer> factory;
    public final int applyTime;

    public HealingEntry(@Nonnull Item item, @Nonnull Function<ItemStack, AbstractPartHealer> factory, int applyTime) {
        this.item = item;
        this.factory = factory;
        this.applyTime = applyTime;
    }
}
