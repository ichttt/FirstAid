package com.creativemd.playerrevive.api;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.CombatEntry;
import net.minecraft.util.CombatTracker;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CombatTrackerClone {
	
	private static final Field combatEntriesField = ReflectionHelper.findField(CombatTracker.class, "combatEntries", "field_94556_a");
	private static final Field lastDamageTimeField = ReflectionHelper.findField(CombatTracker.class, "lastDamageTime", "field_94555_c");
	private static final Field combatStartTimeField = ReflectionHelper.findField(CombatTracker.class, "combatStartTime", "field_152775_d");
	private static final Field combatEndTimeField = ReflectionHelper.findField(CombatTracker.class, "combatEndTime", "field_152776_e");
	private static final Field inCombatField = ReflectionHelper.findField(CombatTracker.class, "inCombat", "field_94552_d");
	private static final Field takingDamageField = ReflectionHelper.findField(CombatTracker.class, "takingDamage", "field_94553_e");
	private static final Field fallSuffixField = ReflectionHelper.findField(CombatTracker.class, "fallSuffix", "field_94551_f");
	
	private final List<CombatEntry> combatEntries = Lists.<CombatEntry>newArrayList();
	private int lastDamageTime;
	private int combatStartTime;
	private int combatEndTime;
	private boolean inCombat;
	private boolean takingDamage;
	private String fallSuffix;
	
	public CombatTrackerClone(CombatTracker tracker) {
		try {
			combatEntries.addAll((Collection<? extends CombatEntry>) combatEntriesField.get(tracker));
			lastDamageTime = lastDamageTimeField.getInt(tracker);
			combatStartTime = combatStartTimeField.getInt(tracker);
			combatEndTime = combatEndTimeField.getInt(tracker);
			inCombat = inCombatField.getBoolean(tracker);
			takingDamage = takingDamageField.getBoolean(tracker);
			fallSuffix = (String) fallSuffixField.get(tracker);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public void overwriteTracker(CombatTracker tracker) {
		try {
			List<CombatEntry> entries = (List<CombatEntry>) combatEntriesField.get(tracker);
			entries.clear();
			entries.addAll(combatEntries);
			lastDamageTimeField.setInt(tracker, lastDamageTime);
			combatStartTimeField.setInt(tracker, combatStartTime);
			combatEndTimeField.setInt(tracker, combatEndTime);
			inCombatField.setBoolean(tracker, inCombat);
			takingDamageField.setBoolean(tracker, takingDamage);
			fallSuffixField.set(tracker, fallSuffix);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
}
