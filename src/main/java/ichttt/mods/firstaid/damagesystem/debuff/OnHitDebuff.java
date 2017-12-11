package ichttt.mods.firstaid.damagesystem.debuff;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.network.MessagePlayHurtSound;
import ichttt.mods.firstaid.sound.EnumHurtSound;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;
import java.util.LinkedHashMap;
import java.util.Map;

public class OnHitDebuff extends AbstractDebuff {
    private final LinkedHashMap<Float, Integer> map = new LinkedHashMap<>();
    private final EnumHurtSound sound;

    public OnHitDebuff(String potionName, BooleanSupplier disableEnableSupplier, @Nonnull EnumHurtSound sound) {
        super(potionName, disableEnableSupplier);
        this.sound = sound;
    }

    public OnHitDebuff addCondition(float damageNeeded, int timeInTicks) {
        map.put(damageNeeded, timeInTicks);
        return this;
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayerMP player) {
        if (!this.isEnabled.getAsBoolean())
            return;
        int value = -1;
        for (Map.Entry entry : map.entrySet()) {
            float key = (float) entry.getKey();
            if (damage >= key) {
                int newValue = (int) entry.getValue();
                value = Math.max(value, newValue);
                player.addPotionEffect(new PotionEffect(effect, newValue, 0, false, false));
            }
        }
        if (value != -1 && sound != EnumHurtSound.NONE)
            FirstAid.NETWORKING.sendTo(new MessagePlayHurtSound(sound, value), player);
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayerMP player) {

    }
}
