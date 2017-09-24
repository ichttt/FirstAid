package de.technikforlife.firstaid.damagesystem.debuff;

import de.technikforlife.firstaid.FirstAidConfig;
import de.technikforlife.firstaid.damagesystem.DamageablePart;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.PlayerDataManager;
import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;

public class SharedDebuff implements IDebuff {
    private final AbstractDebuff debuff;
    private final EnumPlayerPart[] parts;
    private int damage;
    private int healingDone;
    private int damageCount;
    private int healingCount;

    public SharedDebuff(AbstractDebuff debuff, EnumPlayerPart... parts) {
        this.debuff = debuff;
        this.parts = parts;
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayer player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        this.damage += damage;
        this.damageCount++;
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayer player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        this.healingDone += healingDone;
        this.healingCount++;
    }

    public void tick(EntityPlayer player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        if (player.world.isRemote)
            return;
        int count = healingCount + damageCount;
        if (count > 0) {
            PlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
            float healthPerMax = 0;
            for (EnumPlayerPart part : parts) {
                DamageablePart damageablePart = damageModel.getFromEnum(part);
                healthPerMax += damageablePart.currentHealth / damageablePart.maxHealth;
            }
            healthPerMax /= parts.length;
            if (healingCount > 0) {
                this.healingDone /= healingCount;
                debuff.handleHealing(this.healingDone, healthPerMax, player);
            }
            if (damageCount > 0) {
                this.damage /= damageCount;
                debuff.handleDamageTaken(this.damage, healthPerMax, player);
            }
            this.healingDone = 0;
            this.damage = 0;
            this.damageCount = 0;
            this.healingCount = 0;
        }
        if (debuff instanceof ConstantDebuff)
            ((ConstantDebuff) debuff).update(player);
    }
}
