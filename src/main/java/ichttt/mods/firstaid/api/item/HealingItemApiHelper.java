package ichttt.mods.firstaid.api.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Methods that link to internal helper methods
 */
public abstract class HealingItemApiHelper {
    static HealingItemApiHelper INSTANCE;

    /**
     * DO NOT USE! ONLY FOR INTERNAL THINGS
     */
    public static void setImpl(HealingItemApiHelper impl) {
        INSTANCE = impl;
    }

    @Nonnull
    public abstract ActionResult<ItemStack> onItemRightClick(ItemHealing itemHealing, World worldIn, EntityPlayer playerIn, EnumHand handIn);

    @Nonnull
    public abstract CreativeTabs getFirstAidTab();
}
