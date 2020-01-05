package ichttt.mods.firstaid.api.event;

import ichttt.mods.firstaid.api.damagesystem.PlayerDamageModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class FirstAidPlayerDamageEvent extends FirstAidLivingDamageEvent {
    private final EntityPlayer player;
    private final PlayerDamageModel afterDamageDone;
    private final PlayerDamageModel beforeDamageDone;

    public FirstAidPlayerDamageEvent(EntityPlayer entity, PlayerDamageModel afterDamageDone, PlayerDamageModel beforeDamageDone, DamageSource source, float undistributedDamage) {
        super(entity, afterDamageDone, beforeDamageDone, source, undistributedDamage);
        this.player = entity;
        this.afterDamageDone = afterDamageDone;
        this.beforeDamageDone = beforeDamageDone;
    }

    @Override
    public EntityPlayer getEntity() {
        return this.player;
    }

    @Override
    public PlayerDamageModel getAfterDamage() {
        return this.afterDamageDone;
    }

    @Override
    public PlayerDamageModel getBeforeDamage() {
        return this.beforeDamageDone;
    }
}
