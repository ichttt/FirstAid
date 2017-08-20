package de.technikforlife.firstaid.items;

import de.technikforlife.firstaid.FirstAid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemBandage extends Item {

    ItemBandage() {
        setMaxStackSize(16);
        setRegistryName(new ResourceLocation(FirstAid.MODID, "item_bandage"));
        setUnlocalizedName("bandage");
        setCreativeTab(FirstAid.creativeTab);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote)
            player.sendMessage(new TextComponentString("1 Item used"));
        if (!player.isCreative())
        stack.shrink(1);
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
}
