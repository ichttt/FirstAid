package ichttt.mods.firstaid.common.damagesystem.debuff;

import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.floats.Float2IntMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class ConstantDebuff extends AbstractDebuff {
    private int ticks = 0;
    private int activeMultiplier = 0;

    public ConstantDebuff(@Nonnull String potionName, @Nonnull Float2IntLinkedOpenHashMap map, @Nonnull BooleanSupplier isEnabled) {
        super(potionName, map, isEnabled);
    }

    private void syncMultiplier(float healthPerMax) {
        if (!this.isEnabled.getAsBoolean())
            return;
        boolean found = false;
        for (Float2IntMap.Entry entry : map.float2IntEntrySet()) {
            if (healthPerMax < entry.getFloatKey()) {
                ticks = 0;
                activeMultiplier = entry.getIntValue();
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

    @Override
    public void update(EntityPlayer player) {
        this.update(player, -1);
    }

    @Override
    public void update(EntityPlayer player, float healthPerMax) {
        if (!this.isEnabled.getAsBoolean()) return;
        if (activeMultiplier == 0) {
            ticks = 0;
        } else {
            if (ticks == 0) {
                if (healthPerMax != -1)
                    syncMultiplier(healthPerMax); //There are apparently some cases where the multiplier does not sync up right... fix this
                if (activeMultiplier != 0)
                    player.addPotionEffect(new PotionEffect(effect, 240, activeMultiplier - 1, false, false));
            }
            ticks++;
            if (ticks >= 200) ticks = 0;
        }
    }
}
