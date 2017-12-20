package ichttt.mods.firstaid.common.damagesystem.debuff;

import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class SharedDebuff implements IDebuff {
    private final IDebuff debuff;
    private final EnumPlayerPart[] parts;
    private int damage;
    private int healingDone;
    private int damageCount;
    private int healingCount;

    public SharedDebuff(IDebuff debuff, EnumDebuffSlot slot) {
        if (slot.playerParts.length <= 1)
            throw new IllegalArgumentException("Only slots with more then more parts can be wrapped by SharedDebuff!");
        this.debuff = debuff;
        this.parts = slot.playerParts;
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayerMP player) {
        if (debuff.isEnabled()) {
            this.damage += damage;
            this.damageCount++;
        }
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayerMP player) {
        if (debuff.isEnabled()) {
            this.healingDone += healingDone;
            this.healingCount++;
        }
    }

    public void tick(EntityPlayer player) {
        if (!debuff.isEnabled())
            return;
        if (player.world.isRemote || !(player instanceof EntityPlayerMP))
            return;
        int count = healingCount + damageCount;
        if (count > 0) {
            AbstractPlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
            float healthPerMax = 0;
            for (EnumPlayerPart part : parts) {
                AbstractDamageablePart damageablePart = damageModel.getFromEnum(part);
                healthPerMax += damageablePart.currentHealth / damageablePart.getMaxHealth();
            }
            healthPerMax /= parts.length;
            if (healingCount > 0) {
                this.healingDone /= healingCount;
                debuff.handleHealing(this.healingDone, healthPerMax, (EntityPlayerMP) player);
            }
            if (damageCount > 0) {
                this.damage /= damageCount;
                debuff.handleDamageTaken(this.damage, healthPerMax, (EntityPlayerMP) player);
            }
            this.healingDone = 0;
            this.damage = 0;
            this.damageCount = 0;
            this.healingCount = 0;
        }
        debuff.update(player);
    }
}
