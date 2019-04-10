package com.creativemd.playerrevive.api.capability;

import com.creativemd.playerrevive.api.IRevival;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class CapaReviveStorage implements IStorage<IRevival> {
	
	@Override
	public NBTBase writeNBT(Capability<IRevival> capability, IRevival instance, EnumFacing side) {
		return instance.serializeNBT();
	}
	
	@Override
	public void readNBT(Capability<IRevival> capability, IRevival instance, EnumFacing side, NBTBase nbt) {
		instance.deserializeNBT((NBTTagCompound) nbt);
	}
}
