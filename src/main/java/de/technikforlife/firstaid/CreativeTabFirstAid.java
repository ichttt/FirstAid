package de.technikforlife.firstaid;

import de.technikforlife.firstaid.items.FirstAidItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CreativeTabFirstAid extends CreativeTabs{

    public CreativeTabFirstAid() {
        super(FirstAid.MODID);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(FirstAidItems.BANDAGE);
    }
}
