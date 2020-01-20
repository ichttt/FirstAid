package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

public class StandardDamageDistribution extends DamageDistribution {
    private final PlayerDamageDistribution playerDamageDistribution;
    private final GeneralDamageDistribution generalDamageDistribution;

    public StandardDamageDistribution(PlayerDamageDistribution playerDamageDistribution, GeneralDamageDistribution generalDamageDistribution) {
        this.playerDamageDistribution = playerDamageDistribution;
        this.generalDamageDistribution = generalDamageDistribution;
    }

    @Nonnull
    @Override
    protected List<Pair<EntityEquipmentSlot, List<DamageablePart>>> getPartList(EntityDamageModel damageModel, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer)
            return this.playerDamageDistribution.getPartList(damageModel, entity);
        else
            return this.generalDamageDistribution.getPartList(damageModel, entity);
    }
}
