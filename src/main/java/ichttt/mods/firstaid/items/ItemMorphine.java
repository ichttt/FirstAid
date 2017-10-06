package ichttt.mods.firstaid.items;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ItemMorphine extends Item {

    ItemMorphine() {
        setMaxStackSize(16);
        setRegistryName(new ResourceLocation(FirstAid.MODID, "morphine"));
        setUnlocalizedName("morphine");
        setCreativeTab(FirstAid.creativeTab);
    }

    @Override
    @Nonnull
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack, World world, EntityLivingBase entityLiving) {
        if (!world.isRemote && entityLiving instanceof EntityPlayer) {
            PlayerDamageModel damageModel = PlayerDataManager.getDamageModel((EntityPlayer) entityLiving);
            Objects.requireNonNull(damageModel).applyMorphine();
        }
        stack.shrink(1);
        return stack;
    }

    @Override
    @Nonnull
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.EAT;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 40;
    }
}
