package ichttt.mods.firstaid.damagesystem.debuff;

import ichttt.mods.firstaid.FirstAidConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;

import java.util.function.BooleanSupplier;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConstantDebuff extends AbstractDebuff {
    private int ticks = 0;
    private final LinkedHashMap<Float, Integer> map = new LinkedHashMap<>();
    private int activeMultiplier = 0;

    public ConstantDebuff(String potionName, BooleanSupplier disableEnableSupplier) {
        super(potionName, disableEnableSupplier);
    }

    public ConstantDebuff addBound(float value, int multiplier) {
        map.put(value, multiplier);
        return this;
    }

    private void syncMultiplier(float healthPerMax) {
        if (!this.isEnabled.getAsBoolean())
            return;
        boolean found = false;
        for (Map.Entry entry : map.entrySet()) {
            float key = (float) entry.getKey();
            if (healthPerMax < key) {
                int newValue = (int) entry.getValue();
                ticks = 0;
                activeMultiplier = newValue;
                found = true;
                break;
            }
        }
        if (!found)
            activeMultiplier = 0;
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayerMP player) {
        syncMultiplier(healthPerMax);
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayerMP player) {
        syncMultiplier(healthPerMax);
    }

    public void update(EntityPlayer player) {
        if (!this.isEnabled.getAsBoolean())
            return;
        if (activeMultiplier == 0) {
            ticks = 0;
        } else {
            if (ticks == 0)
                player.addPotionEffect(new PotionEffect(effect, 200, activeMultiplier - 1, false, false));
            ticks++;
            if (ticks >= 140)
                ticks = 0;
        }
    }
}
