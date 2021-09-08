/*
 * FirstAid
 * Copyright (C) 2017-2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.common.util;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.AABBAlignedBoundingBox;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistribution;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlayerSizeHelper {
    private static final Map<EquipmentSlot, AABBAlignedBoundingBox> NORMAL_BOXES;
    private static final Map<EquipmentSlot, AABBAlignedBoundingBox> SNEAKING_BOXES;


    static {
        Map<EquipmentSlot, AABBAlignedBoundingBox> builder = new LinkedHashMap<>();
        builder.put(EquipmentSlot.FEET, new AABBAlignedBoundingBox(0D, 0D, 0D, 1D, 0.15D, 1D));
        builder.put(EquipmentSlot.LEGS, new AABBAlignedBoundingBox(0D, 0.15D, 0D, 1D, 0.45D, 1D));
        builder.put(EquipmentSlot.CHEST, new AABBAlignedBoundingBox(0D, 0.45D, 0D, 1D, 0.8D, 1D));
        builder.put(EquipmentSlot.HEAD, new AABBAlignedBoundingBox(0D, 0.8D, 0D, 1D, 1D, 1D));
        NORMAL_BOXES = Collections.unmodifiableMap(builder);

        builder = new LinkedHashMap<>();
        builder.put(EquipmentSlot.FEET,  new AABBAlignedBoundingBox(0D, 0D, 0D, 1D, 0.15D, 1D));
        builder.put(EquipmentSlot.LEGS, new AABBAlignedBoundingBox(0D, 0.15D, 0D, 1D, 0.4D, 1D));
        builder.put(EquipmentSlot.CHEST, new AABBAlignedBoundingBox(0D, 0.4D, 0D, 1D, 0.75D, 1D));
        builder.put(EquipmentSlot.HEAD,  new AABBAlignedBoundingBox(0D, 0.75D, 0D, 1D, 1D, 1D));
        SNEAKING_BOXES = Collections.unmodifiableMap(builder);
    }

    @Nonnull
    public static Map<EquipmentSlot, AABBAlignedBoundingBox> getBoxes(Entity entity) {
        switch (entity.getPose()) {
            case STANDING:
                return NORMAL_BOXES;
            case CROUCHING:
                return SNEAKING_BOXES;
            case SPIN_ATTACK: //tridant
            case FALL_FLYING: //elytra
                return Collections.emptyMap(); // To be evaluated
            case DYING:
            case SLEEPING:
            case SWIMMING:
            default:
                return Collections.emptyMap();
        }
    }

    public static EquipmentSlot getSlotTypeForProjectileHit(Entity hittingObject, Player toTest) {
        Map<EquipmentSlot, AABBAlignedBoundingBox> toUse = getBoxes(toTest);
        Vec3 oldPosition = hittingObject.position();
        Vec3 newPosition = oldPosition.add(hittingObject.getDeltaMovement());

        // See ProjectileHelper.getEntityHitResult
        float[] inflationSteps = new float[] {0.01F, 0.1F, 0.2F, 0.3F};
        for (float inflation : inflationSteps) {
            EquipmentSlot bestSlot = null;
            double bestValue = Double.MAX_VALUE;
            for (Map.Entry<EquipmentSlot, AABBAlignedBoundingBox> entry : toUse.entrySet()) {
                AABB axisalignedbb = entry.getValue().createAABB(toTest.getBoundingBox()).inflate(inflation);
                Optional<Vec3> optional = axisalignedbb.clip(oldPosition, newPosition);
                if (optional.isPresent()) {
                    double d1 = oldPosition.distanceToSqr(optional.get());
                    double d2 = 0D;//newPosition.distanceToSqr(optional.get());
                    if ((d1 + d2) < bestValue) {
                        bestSlot = entry.getKey();
                        bestValue = d1 + d2;
                    }
                }
            }
            if (bestSlot != null) {
                if (FirstAidConfig.GENERAL.debug.get()) {
                    FirstAid.LOGGER.info("getSlotTypeForProjectileHit: Inflation: " + inflation + " best slot: " + bestSlot);
                }
                return bestSlot;
            }
        }
        if (FirstAidConfig.GENERAL.debug.get()) {
            FirstAid.LOGGER.info("getSlotTypeForProjectileHit: Not found!");
        }
        return null;
    }


    public static IDamageDistribution getMeleeDistribution(Player player, DamageSource source) {
        Entity causingEntity = source.getEntity();
        if (causingEntity != null && causingEntity == source.getDirectEntity() && causingEntity instanceof Mob) {
            Mob mobEntity = (Mob) causingEntity;
            if (mobEntity.getTarget() == player && mobEntity.goalSelector.getRunningGoals().anyMatch(prioritizedGoal -> prioritizedGoal.getGoal() instanceof MeleeAttackGoal)) {
                Map<EquipmentSlot, AABBAlignedBoundingBox> boxes = PlayerSizeHelper.getBoxes(player);
                if (!boxes.isEmpty()) {
                    List<EquipmentSlot> allowedParts = new ArrayList<>();
                    AABB modAABB = mobEntity.getBoundingBox().inflate(mobEntity.getBbWidth() * 2F + player.getBbWidth(), 0, mobEntity.getBbWidth() * 2F + player.getBbWidth());
                    for (Map.Entry<EquipmentSlot, AABBAlignedBoundingBox> entry : boxes.entrySet()) {
                        AABB partAABB = entry.getValue().createAABB(player.getBoundingBox());
                        if (modAABB.intersects(partAABB)) {
                            allowedParts.add(entry.getKey());
                        }
                    }
                    if (FirstAidConfig.GENERAL.debug.get()) {
                        FirstAid.LOGGER.info("getMeleeDistribution: Has distribution with {}", allowedParts);
                    }
                    if (allowedParts.isEmpty() && player.getY() > mobEntity.getY() && (player.getY() - mobEntity.getY()) < mobEntity.getBbHeight() * 2F) {
                        // HACK: y is at the bottom of the aabb of mobs, so the range of mobs to your feet is larger than the range of them to your head
                        // If no matching region can be found, but the y difference is within 2 times the bb height of the attacking mob
                        // This should be accurate enough (in theory)
                        if (FirstAidConfig.GENERAL.debug.get()) {
                            FirstAid.LOGGER.info("Hack adding feet");
                        }
                        allowedParts.add(EquipmentSlot.FEET);
                    }
                    if (!allowedParts.isEmpty() && !allowedParts.containsAll(Arrays.asList(CommonUtils.ARMOR_SLOTS))) {
                        List<Pair<EquipmentSlot, EnumPlayerPart[]>> list = new ArrayList<>();
                        for (EquipmentSlot allowedPart : allowedParts) {
                            list.add(Pair.of(allowedPart, CommonUtils.getPartArrayForSlot(allowedPart)));
                        }
                        return new StandardDamageDistribution(list, true, true);
                    }
                }
            }
        }
        return null;
    }

}
