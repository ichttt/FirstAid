package de.technikforlife.firstaid.items;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.network.MessageReceiveDamageInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ItemBandage extends Item {

    ItemBandage() {
        setMaxStackSize(16);
        setRegistryName(new ResourceLocation(FirstAid.MODID, "bandage"));
        setUnlocalizedName("bandage");
        setCreativeTab(FirstAid.creativeTab);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        if (!world.isRemote) {
            PlayerDamageModel damageModel = Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null));
            FirstAid.NETWORKING.sendTo(new MessageReceiveDamageInfo(damageModel), (EntityPlayerMP) player);
        }

        return super.onItemRightClick(world, player, hand);
    }
}
