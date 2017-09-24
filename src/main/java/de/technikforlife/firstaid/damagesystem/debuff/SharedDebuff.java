package de.technikforlife.firstaid.damagesystem.debuff;

import de.technikforlife.firstaid.FirstAidConfig;
import net.minecraft.entity.player.EntityPlayer;

public class SharedDebuff implements IDebuff {
    private final AbstractDebuff debuff;
    private int damage;
    private int healthPerMax;
    private int healingDone;
    private int damageCount;
    private int healingCount;

    public SharedDebuff(AbstractDebuff debuff) {
        this.debuff = debuff;
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayer player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        this.damage += damage;
        this.healthPerMax += healthPerMax;
        this.damageCount++;
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayer player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        this.healingDone += healingDone;
        this.healthPerMax += healthPerMax;
        this.healingCount++;
    }

    public void tick(EntityPlayer player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        if (player.world.isRemote)
            return;
        int count = healingCount + damageCount;
        if (count > 0) {
            this.healthPerMax /= (healingCount + damageCount);
            if (healingCount > 0) {
                this.healingDone /= healingCount;
                debuff.handleHealing(this.healingDone, this.healthPerMax, player);
            }
            if (damageCount > 0) {
                this.damage /= damageCount;
                debuff.handleDamageTaken(this.damage, this.healthPerMax, player);
            }
            this.healingDone = 0;
            this.damage = 0;
            this.healthPerMax = 0;
            this.damageCount = 0;
            this.healingCount = 0;
        }
        if (debuff instanceof ConstantDebuff)
            ((ConstantDebuff) debuff).update(player);
    }
}
