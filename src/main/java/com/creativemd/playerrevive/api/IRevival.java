package com.creativemd.playerrevive.api;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.INBTSerializable;

public interface IRevival extends INBTSerializable<NBTTagCompound> {
	
	void tick();
	
	boolean isHealty();
	
	void stopBleeding();
	
	void startBleeding(EntityPlayer player, DamageSource source);
	
	float getProgress();
	
	boolean isRevived();
	
	boolean isDead();
	
	int getTimeLeft();
	
	void kill();
	
	DamageSource getSource();
	
	CombatTrackerClone getTrackerClone();
	
	List<EntityPlayer> getRevivingPlayers();
}
