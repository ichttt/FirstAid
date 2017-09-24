package de.technikforlife.firstaid.damagesystem.debuff;

import de.technikforlife.firstaid.FirstAidConfig;
import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.floats.Float2IntMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;

public class ConstantDebuff extends AbstractDebuff {
    private int ticks = 0;
    private final Float2IntLinkedOpenHashMap map = new Float2IntLinkedOpenHashMap();
    private int activeMultiplier = 0;

    public ConstantDebuff(String potionName) {
        super(potionName);
    }

    public ConstantDebuff addBound(float value, int multiplier) {
        map.put(value, multiplier);
        return this;
    }

    private void syncMultiplier(float healthPerMax) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        boolean found = false;
        for (Float2IntMap.Entry entry : map.float2IntEntrySet()) {
            if (healthPerMax < entry.getFloatKey()) {
                ticks = 0;
                activeMultiplier = entry.getIntValue();
                System.out.println("Setting active multiplier");
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Resetting active multiplier");
            activeMultiplier = 0;
        }
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayer player) {
        syncMultiplier(healthPerMax);
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayer player) {
        syncMultiplier(healthPerMax);
    }

    public void update(EntityPlayer player) {
        if (!FirstAidConfig.enableDebuffs)
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
