package ichttt.mods.firstaid.common.util;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.AABBAlignedBoundingBox;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistribution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
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
    private static final Map<EquipmentSlotType, AABBAlignedBoundingBox> NORMAL_BOXES;
    private static final Map<EquipmentSlotType, AABBAlignedBoundingBox> SNEAKING_BOXES;


    static {
        Map<EquipmentSlotType, AABBAlignedBoundingBox> builder = new LinkedHashMap<>();
        builder.put(EquipmentSlotType.FEET, new AABBAlignedBoundingBox(0D, 0D, 0D, 1D, 0.15D, 1D));
        builder.put(EquipmentSlotType.LEGS, new AABBAlignedBoundingBox(0D, 0.15D, 0D, 1D, 0.45D, 1D));
        builder.put(EquipmentSlotType.CHEST, new AABBAlignedBoundingBox(0D, 0.45D, 0D, 1D, 0.8D, 1D));
        builder.put(EquipmentSlotType.HEAD, new AABBAlignedBoundingBox(0D, 0.8D, 0D, 1D, 1D, 1D));
        NORMAL_BOXES = Collections.unmodifiableMap(builder);

        builder = new LinkedHashMap<>();
        builder.put(EquipmentSlotType.FEET,  new AABBAlignedBoundingBox(0D, 0D, 0D, 1D, 0.15D, 1D));
        builder.put(EquipmentSlotType.LEGS, new AABBAlignedBoundingBox(0D, 0.15D, 0D, 1D, 0.4D, 1D));
        builder.put(EquipmentSlotType.CHEST, new AABBAlignedBoundingBox(0D, 0.4D, 0D, 1D, 0.75D, 1D));
        builder.put(EquipmentSlotType.HEAD,  new AABBAlignedBoundingBox(0D, 0.75D, 0D, 1D, 1D, 1D));
        SNEAKING_BOXES = Collections.unmodifiableMap(builder);
    }

    @Nonnull
    public static Map<EquipmentSlotType, AABBAlignedBoundingBox> getBoxes(Entity entity) {
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

    public static EquipmentSlotType getSlotTypeForProjectileHit(Entity hittingObject, PlayerEntity toTest) {
        Map<EquipmentSlotType, AABBAlignedBoundingBox> toUse = getBoxes(toTest);
        Vector3d oldPosition = hittingObject.position();
        Vector3d newPosition = oldPosition.add(hittingObject.getDeltaMovement());

        // See ProjectileHelper.getEntityHitResult
        float[] inflationSteps = new float[] {0.01F, 0.1F, 0.2F, 0.3F};
        for (float inflation : inflationSteps) {
            EquipmentSlotType bestSlot = null;
            double bestValue = Double.MAX_VALUE;
            for (Map.Entry<EquipmentSlotType, AABBAlignedBoundingBox> entry : toUse.entrySet()) {
                AxisAlignedBB axisalignedbb = entry.getValue().createAABB(toTest.getBoundingBox()).inflate(inflation);
                Optional<Vector3d> optional = axisalignedbb.clip(oldPosition, newPosition);
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


    public static IDamageDistribution getMeleeDistribution(PlayerEntity player, DamageSource source) {
        Entity causingEntity = source.getEntity();
        if (causingEntity != null && causingEntity == source.getDirectEntity() && causingEntity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity) causingEntity;
            if (mobEntity.getTarget() == player && mobEntity.goalSelector.getRunningGoals().anyMatch(prioritizedGoal -> prioritizedGoal.getGoal() instanceof MeleeAttackGoal)) {
                Map<EquipmentSlotType, AABBAlignedBoundingBox> boxes = PlayerSizeHelper.getBoxes(player);
                if (!boxes.isEmpty()) {
                    List<EquipmentSlotType> allowedParts = new ArrayList<>();
                    AxisAlignedBB modAABB = mobEntity.getBoundingBox().inflate(mobEntity.getBbWidth() * 2F + player.getBbWidth(), 0, mobEntity.getBbWidth() * 2F + player.getBbWidth());
                    for (Map.Entry<EquipmentSlotType, AABBAlignedBoundingBox> entry : boxes.entrySet()) {
                        AxisAlignedBB partAABB = entry.getValue().createAABB(player.getBoundingBox());
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
                        allowedParts.add(EquipmentSlotType.FEET);
                    }
                    if (!allowedParts.isEmpty() && !allowedParts.containsAll(Arrays.asList(CommonUtils.ARMOR_SLOTS))) {
                        List<Pair<EquipmentSlotType, EnumPlayerPart[]>> list = new ArrayList<>();
                        for (EquipmentSlotType allowedPart : allowedParts) {
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
