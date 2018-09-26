package ichttt.mods.firstaid.common.items;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.damagesystem.PartHealer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(FirstAid.MODID)
public class FirstAidItems {
    public static final Item BANDAGE = getNull();
    public static final Item PLASTER = getNull();
    public static final Item MORPHINE = getNull();

    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private static <T> T getNull() {
        return null;
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        registry.register(new DefaultItemHealing("bandage", stack -> new PartHealer(18 * 20, 4, stack), 2500));
        registry.register(new DefaultItemHealing("plaster", stack -> new PartHealer(22 * 20, 2, stack), 3000));
        registry.register(new ItemMorphine());
    }
}
