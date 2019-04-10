package com.creativemd.playerrevive.api.capability;

import java.util.List;

import com.creativemd.playerrevive.api.CombatTrackerClone;
import com.creativemd.playerrevive.api.IRevival;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapaRevive {
	
	@CapabilityInject(IRevival.class)
	public static Capability<IRevival> reviveCapa = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IRevival.class, new CapaReviveStorage(), DefaultImpl::new);
	}
	
	public static class DefaultImpl implements IRevival {
		
		@Override
		public void tick() {
		}
		
		@Override
		public boolean isHealty() {
			return false;
		}
		
		@Override
		public void stopBleeding() {
		}
		
		@Override
		public void startBleeding(EntityPlayer player, DamageSource source) {
		}
		
		@Override
		public float getProgress() {
			return 0;
		}
		
		@Override
		public boolean isRevived() {
			return false;
		}
		
		@Override
		public boolean isDead() {
			return false;
		}
		
		@Override
		public int getTimeLeft() {
			return 0;
		}
		
		@Override
		public void kill() {
		}
		
		@Override
		public List<EntityPlayer> getRevivingPlayers() {
			return null;
		}
		
		@Override
		public NBTTagCompound serializeNBT() {
			return null;
		}
		
		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
		}
		
		@Override
		public DamageSource getSource() {
			return null;
		}
		
		@Override
		public CombatTrackerClone getTrackerClone() {
			return null;
		}
	}
}
