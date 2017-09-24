package de.technikforlife.firstaid.damagesystem.debuff;

import de.technikforlife.firstaid.FirstAidConfig;
import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.floats.Float2IntMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;

public class OnHitDebuff extends AbstractDebuff {
    private final Float2IntLinkedOpenHashMap map = new Float2IntLinkedOpenHashMap();

    public OnHitDebuff(String potionName) {
        super(potionName);
    }

    public OnHitDebuff addCondition(float damageNeeded, int timeInTicks) {
        map.put(damageNeeded, timeInTicks);
        return this;
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayer player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        for (Float2IntMap.Entry entry : map.float2IntEntrySet()) {
            if (damage >= entry.getFloatKey())
                player.addPotionEffect(new PotionEffect(effect, entry.getIntValue(), 0, false, false));
        }
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayer player) {

    }
}
