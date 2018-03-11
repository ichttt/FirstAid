package ichttt.mods.firstaid.common.damagesystem.capability;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class CapProvider implements ICapabilitySerializable<NBTTagCompound> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(FirstAid.MODID, "capExtendedHealthSystem");

    private final WeakReference<EntityPlayer> player;

    public CapProvider(EntityPlayer player, AbstractPlayerDamageModel damageModel) {
        this.player = new WeakReference<>(player);
        PlayerDataManager.put(player, damageModel);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return player.get() != null && capability == CapabilityExtendedHealthSystem.INSTANCE;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        EntityPlayer player = this.player.get();
        if (player != null && capability == CapabilityExtendedHealthSystem.INSTANCE)
            return (T) getModel();
        return null;
    }

    @Nullable
    private AbstractPlayerDamageModel getModel() {
        EntityPlayer player = this.player.get();
        if (player == null)
            return null;
        return PlayerDataManager.getDamageModel(player);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        AbstractPlayerDamageModel playerDamageModel = getModel();
        if (playerDamageModel == null)
            return new NBTTagCompound();
        return playerDamageModel.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        AbstractPlayerDamageModel playerDamageModel = getModel();
        if (playerDamageModel != null)
            playerDamageModel.deserializeNBT(nbt);
    }
}
