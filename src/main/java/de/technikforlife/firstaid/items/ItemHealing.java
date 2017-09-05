package de.technikforlife.firstaid.items;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.network.MessageReceiveDamageInfoWithItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ItemHealing extends Item {
    private final EnumHealingType type;

    ItemHealing(String name, EnumHealingType type) {
        this.type = type;
        setMaxStackSize(16);
        setRegistryName(new ResourceLocation(FirstAid.MODID, name));
        setUnlocalizedName(name);
        setCreativeTab(FirstAid.creativeTab);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        if (world.isRemote) {
            FirstAid.proxy.showGuiApplyHealth();
        } else {
            PlayerDamageModel damageModel = Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null));
            FirstAid.NETWORKING.sendTo(new MessageReceiveDamageInfoWithItem(damageModel, type, hand), (EntityPlayerMP) player);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
