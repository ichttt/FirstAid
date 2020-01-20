package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GeneralDamageDistribution extends DamageDistribution {
    private final List<EntityEquipmentSlot> partList;
    private final List<List<PreferredDamageDistribution>> unusedNeighbours;
    private final boolean shuffle;

    /**
     * Precompute as much as possible
     */
    public GeneralDamageDistribution(boolean shuffle, EntityEquipmentSlot... slots) {
        List<EntityEquipmentSlot> parts = Arrays.asList(slots);
        this.partList = shuffle ? new ArrayList<>(parts) : parts;
        this.shuffle = shuffle;
        if (shuffle && slots.length <= 1)
            throw new IllegalArgumentException("Shuffle doesn't make sense with " + Arrays.toString(slots));


        if (shuffle) {
            List<PreferredDamageDistribution> subSlots = new ArrayList<>(CommonUtils.ARMOR_SLOTS.length - parts.size());
            for (EntityEquipmentSlot slot : CommonUtils.ARMOR_SLOTS) {
                if (!partList.contains(slot))
                    subSlots.add(new PreferredDamageDistribution(slot));
            }
            unusedNeighbours = Collections.singletonList(subSlots);
        } else {
            this.unusedNeighbours = new ArrayList<>(CommonUtils.ARMOR_SLOTS.length - parts.size());
            EntityEquipmentSlot slot = partList.get(partList.size() - 1);
            int index = slot.getIndex();
            for (int i = 0; i < CommonUtils.ARMOR_SLOTS.length; i++) {
                int upIndex = index + i;
                int downIndex = index - i;
                if (upIndex < CommonUtils.ARMOR_SLOTS.length && downIndex >= 0) {
                    List<PreferredDamageDistribution> subSlots = new ArrayList<>(2);
                    subSlots.add(new PreferredDamageDistribution(CommonUtils.ARMOR_SLOTS[upIndex]));
                    subSlots.add(new PreferredDamageDistribution(CommonUtils.ARMOR_SLOTS[downIndex]));
                    unusedNeighbours.add(subSlots);
                } else if (upIndex < CommonUtils.ARMOR_SLOTS.length) {
                    unusedNeighbours.add(Collections.singletonList(new PreferredDamageDistribution(CommonUtils.ARMOR_SLOTS[upIndex])));
                } else if (downIndex >= 0) {
                    unusedNeighbours.add(Collections.singletonList(new PreferredDamageDistribution(CommonUtils.ARMOR_SLOTS[downIndex])));
                }
            }
        }
    }

    @Nonnull
    @Override
    protected List<Pair<EntityEquipmentSlot, List<DamageablePart>>> getPartList(EntityDamageModel damageModel, EntityLivingBase entity) {
        if (shuffle) Collections.shuffle(partList);

        List<Pair<EntityEquipmentSlot, List<DamageablePart>>> list = new ArrayList<>(partList.size());
        for (EntityEquipmentSlot slot : partList) {
            list.add(Pair.of(slot, damageModel.getParts(slot)));
        }
        return list;
    }

    @Override
    public float distributeDamage(float damage, @Nonnull EntityLivingBase entity, @Nonnull DamageSource source, boolean addStat) {
        float rest = super.distributeDamage(damage, entity, source, addStat);
        if (rest > 0) {
            outerLoop : for (List<PreferredDamageDistribution> distributions : unusedNeighbours) {
                if (distributions.size() > 1)
                    Collections.shuffle(distributions);
                for (PreferredDamageDistribution distribution : distributions) {
                    rest = distribution.distributeDamage(rest, entity, source, addStat);
                    if (rest <= 0F)
                        break outerLoop;
                }
            }
        }
        return rest;
    }
}
