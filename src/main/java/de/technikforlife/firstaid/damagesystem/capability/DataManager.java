package de.technikforlife.firstaid.damagesystem.capability;

import com.google.common.collect.MapMaker;
import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentMap;

public class DataManager implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(FirstAid.MODID, "capExtendedHealthSystem");
    private static final ConcurrentMap<EntityPlayer, PlayerDamageModel> capList = new MapMaker().concurrencyLevel(2).weakKeys().makeMap();

    private final EntityPlayer player;

    public DataManager(EntityPlayer player) {
        this.player = player;
        capList.putIfAbsent(player, new PlayerDamageModel());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return (T) capList.get(player);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        PlayerDamageModel damageModel = capList.get(player);
        return damageModel.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        PlayerDamageModel damageModel = capList.get(player);
        damageModel.deserializeNBT(nbt);
    }

    public static void tickAll(World world) {
        capList.forEach((player, playerDamageModel) -> playerDamageModel.tick(world));
    }
}
