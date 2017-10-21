package ichttt.mods.firstaid.damagesystem.debuff;

import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.damagesystem.DamageablePart;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.damagesystem.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

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
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayerMP player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        this.damage += damage;
        this.damageCount++;
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayerMP player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        this.healingDone += healingDone;
        this.healingCount++;
    }

    public void tick(EntityPlayer player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        if (player.world.isRemote || !(player instanceof EntityPlayerMP))
            return;
        int count = healingCount + damageCount;
        if (count > 0) {
            PlayerDamageModel damageModel = PlayerDataManager.getDamageModel(player);
            float healthPerMax = 0;
            for (EnumPlayerPart part : parts) {
                DamageablePart damageablePart = damageModel.getFromEnum(part);
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
        if (debuff instanceof ConstantDebuff)
            ((ConstantDebuff) debuff).update(player);
    }
}
