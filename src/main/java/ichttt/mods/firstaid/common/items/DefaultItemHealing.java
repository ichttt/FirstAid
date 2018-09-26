package ichttt.mods.firstaid.common.items;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.item.ItemHealing;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class DefaultItemHealing extends ItemHealing {

    DefaultItemHealing(String name, Function<ItemStack, AbstractPartHealer> healerFunction, int applyTime) {
        super(healerFunction, applyTime);
        setMaxStackSize(16);
        setRegistryName(new ResourceLocation(FirstAid.MODID, name));
        setTranslationKey(name);
    }
}
