package ichttt.mods.firstaid.common.items;

import ichttt.mods.firstaid.FirstAid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemHealing extends Item {

    ItemHealing(String name) {
        setMaxStackSize(16);
        setRegistryName(new ResourceLocation(FirstAid.MODID, name));
        setUnlocalizedName(name);
        setCreativeTab(FirstAid.creativeTab);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        if (world.isRemote) FirstAid.proxy.showGuiApplyHealth(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
