package ichttt.mods.firstaid.api.item;

import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

/**
 * Base class for custom healing items. Handles the logic for healing automatically.
 * Registry name, stack size and other behavior can be specified individually.
 * Will automatically register this item to the registry as well
 */
public class ItemHealing extends Item {

    /**
     * Creates a new healing item and registers it to the registry.
     * @param time The time it takes in the GUI in ms
     * @param healerFunction The function to create a new healer from the GUI
     */
    public ItemHealing(Function<ItemStack, AbstractPartHealer> healerFunction, int time) {
        setCreativeTab(HealingItemApiHelper.INSTANCE.getFirstAidTab());
        Objects.requireNonNull(FirstAidRegistry.getImpl(), "FirstAid not loaded or not present!").registerHealingType(this, healerFunction, time);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        return HealingItemApiHelper.INSTANCE.onItemRightClick(this, worldIn, playerIn, handIn);
    }
}
