package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import ichttt.mods.firstaid.api.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerDamageDistribution extends DamageDistribution {
    private final List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList;
    private final boolean shuffle;

    public PlayerDamageDistribution(boolean shuffle, List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList) {
        this.partList = partList;
        for (Pair<EntityEquipmentSlot, EnumPlayerPart[]> pair : partList) {
            for (EnumPlayerPart part : pair.getRight()) {
                if (part.slot != pair.getLeft())
                    throw new RuntimeException(part + " is not a member of " + pair.getLeft());
            }
        }
        this.shuffle = shuffle;
        if (shuffle && partList.size() <= 1)
            throw new IllegalArgumentException("Shuffle doesn't make sense with " + Arrays.toString(partList.toArray()));
    }

    @Nonnull
    @Override
    protected List<Pair<EntityEquipmentSlot, List<DamageablePart>>> getPartList(EntityDamageModel damageModel, EntityLivingBase entity) {
        if (this.shuffle) Collections.shuffle(this.partList);
        PlayerDamageModel playerDamageModel = (PlayerDamageModel) damageModel;
        List<Pair<EntityEquipmentSlot, List<DamageablePart>>> list = new ArrayList<>(partList.size());
        for (Pair<EntityEquipmentSlot, EnumPlayerPart[]> pair : partList) {
            list.add(Pair.of(pair.getLeft(), Arrays.stream(pair.getRight()).map(playerDamageModel::getFromEnum).collect(Collectors.toList())));
        }
        return list;
    }

    @Override
    public float distributeDamage(float damage, @Nonnull EntityLivingBase entity, @Nonnull DamageSource source, boolean addStat) {
        float rest = super.distributeDamage(damage, entity, source, addStat);
        if (rest > 0) {
            EnumPlayerPart[] parts = partList.get(partList.size() - 1).getRight();
            Optional<EnumPlayerPart> playerPart = Arrays.stream(parts).filter(enumPlayerPart -> !enumPlayerPart.getNeighbours().isEmpty()).findAny();
            if (playerPart.isPresent()) {
                List<EnumPlayerPart> neighbours = playerPart.get().getNeighbours();
                neighbours = neighbours.stream().filter(part -> partList.stream().noneMatch(pair -> Arrays.stream(pair.getRight()).anyMatch(p2 -> p2 == part))).collect(Collectors.toList());
                for (EnumPlayerPart part : neighbours)
                    rest = new PreferredDamageDistribution(part).distributeDamage(rest, entity, source, addStat);
            }
        }
        return rest;
    }
}
