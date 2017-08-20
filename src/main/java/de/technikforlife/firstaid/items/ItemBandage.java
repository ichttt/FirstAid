package de.technikforlife.firstaid.items;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.FirstAidConfig;
import de.technikforlife.firstaid.client.GuiShowWounds;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemBandage extends Item {

    ItemBandage() {
        setMaxStackSize(16);
        setRegistryName(new ResourceLocation(FirstAid.MODID, "item_bandage"));
        setUnlocalizedName("bandage");
        setCreativeTab(FirstAid.creativeTab);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        if (world.isRemote)
            Minecraft.getMinecraft().displayGuiScreen(new GuiShowWounds(player));

        return super.onItemRightClick(world, player, hand);
//        EnumActionResult result = player.getHealth() == player.getMaxHealth() ? EnumActionResult.FAIL : EnumActionResult.SUCCESS;
//        player.setActiveHand(hand);
//        return new ActionResult<>(result, player.getHeldItem(hand));
    }

    @Override
    @Nonnull
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack, World world, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            if (!player.isCreative())
                stack.shrink(1);
        }
        else
            stack.shrink(1);
        entityLiving.heal((float) FirstAidConfig.healAmount);
        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }
}
