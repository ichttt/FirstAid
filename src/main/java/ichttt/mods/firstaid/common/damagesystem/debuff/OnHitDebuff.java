package ichttt.mods.firstaid.common.damagesystem.debuff;

import gnu.trove.iterator.TFloatIntIterator;
import gnu.trove.map.TFloatIntMap;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.network.MessagePlayHurtSound;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

public class OnHitDebuff extends AbstractDebuff {
    @Nullable
    private final SoundEvent sound;

    public OnHitDebuff(@Nonnull String potionName, @Nonnull TFloatIntMap map, @Nonnull BooleanSupplier isEnabled, @Nullable SoundEvent sound) {
        super(potionName, map, isEnabled);
        this.sound = sound;
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayerMP player) {
        if (!this.isEnabled.getAsBoolean())
            return;
        int value = -1;
        TFloatIntIterator iterator = map.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            if (damage >= iterator.key()) {
                value = Math.max(value, iterator.value());
                player.addPotionEffect(new PotionEffect(effect, iterator.value(), 0, false, false));
            }
        }
        if (value != -1 && sound != null)
            FirstAid.NETWORKING.sendTo(new MessagePlayHurtSound(sound, value), player);
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayerMP player) {

    }
}
