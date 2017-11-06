package ichttt.mods.firstaid.damagesystem.capability;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(FirstAid.MODID, "capExtendedHealthSystem");

    private final EntityPlayer player;

    public CapProvider(EntityPlayer player, AbstractPlayerDamageModel damageModel) {
        this.player = player;
        PlayerDataManager.capList.put(player, damageModel);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityExtendedHealthSystem.INSTANCE;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityExtendedHealthSystem.INSTANCE)
            return (T) PlayerDataManager.capList.get(player);
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        AbstractPlayerDamageModel damageModel = PlayerDataManager.capList.get(player);
        return damageModel.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        AbstractPlayerDamageModel damageModel = PlayerDataManager.capList.get(player);
        damageModel.deserializeNBT(nbt);
    }
}
