package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.item.HealingItemApiHelper;
import ichttt.mods.firstaid.api.item.ItemHealing;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class HealingItemApiHelperImpl extends HealingItemApiHelper {
    private static final HealingItemApiHelperImpl INSTANCE = new HealingItemApiHelperImpl();

    public static void init() {
        HealingItemApiHelper.setImpl(INSTANCE);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemHealing itemHealing, World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote)
            FirstAid.proxy.showGuiApplyHealth(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Nonnull
    @Override
    public CreativeTabs getFirstAidTab() {
        return FirstAid.CREATIVE_TAB;
    }
}
