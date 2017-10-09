package ichttt.mods.firstaid.damagesystem.capability;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityExtendedHealthSystem {

    @SuppressWarnings("unused")
    @CapabilityInject(PlayerDamageModel.class)
    public static Capability<PlayerDamageModel> INSTANCE;

    public static void register() {
        FirstAid.logger.debug("Registering capability");
        CapabilityManager.INSTANCE.register(PlayerDamageModel.class, new Capability.IStorage<PlayerDamageModel>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<PlayerDamageModel> capability, PlayerDamageModel instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<PlayerDamageModel> capability, PlayerDamageModel instance, EnumFacing side, NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
        }
        , PlayerDamageModel::create);
    }
}
