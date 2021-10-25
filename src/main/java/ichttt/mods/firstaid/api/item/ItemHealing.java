/*
 * FirstAid API
 * Copyright (c) 2017-2021
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package ichttt.mods.firstaid.api.item;

import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.client.ClientHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

import static net.minecraft.util.ActionResultType.CONSUME;

/**
 * Base class for custom healing items. Handles the logic for healing automatically.
 * Registry name, stack size and other behavior can be specified individually.
 * Will automatically register this item to the registry as well
 */
public class ItemHealing extends Item {

    /**
     * Creates a new healing item, sets its tab to the first aid tab, and registers it to the registry.
     * @param time The time it takes in the GUI in ms
     * @param healerFunction The function to create a new healer from the GUI
     */
    public static ItemHealing create(Item.Properties builder, ResourceLocation registryName, Function<ItemStack, AbstractPartHealer> healerFunction, Function<ItemStack, Integer> time) {
        builder.tab(HealingItemApiHelper.INSTANCE.getFirstAidTab());
        return createNoTab(builder, registryName, healerFunction, time);
    }

    /**
     * Creates a new healing item, and registers it to the registry.
     * @param time The time it takes in the GUI in ms
     * @param healerFunction The function to create a new healer from the GUI
     */
    public static ItemHealing createNoTab(Item.Properties builder, ResourceLocation registryName, Function<ItemStack, AbstractPartHealer> healerFunction, Function<ItemStack, Integer> time) {
         ItemHealing itemHealing = new ItemHealing(builder, healerFunction, time);
         itemHealing.setRegistryName(registryName);
         return itemHealing;
    }


    protected ItemHealing(Item.Properties builder, Function<ItemStack, AbstractPartHealer> healerFunction, Function<ItemStack, Integer> time) {
        super(builder);
        Objects.requireNonNull(FirstAidRegistry.getImpl(), "FirstAid not loaded or not present!").registerHealingType(this, healerFunction, time);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, @Nonnull Hand handIn) {
        return HealingItemApiHelper.INSTANCE.onItemRightClick(this, worldIn, playerIn, handIn);
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity playerEntity, LivingEntity livingEntity, Hand hand) {
        if (livingEntity instanceof PlayerEntity && playerEntity.isLocalPlayer()) {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientHooks.showGuiApplyHealth(hand, (PlayerEntity) livingEntity));
            return CONSUME;
        }
        return super.interactLivingEntity(stack, playerEntity, livingEntity, hand);
    }
}
