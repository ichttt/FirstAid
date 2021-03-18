package ichttt.mods.firstaid.common.util;

import com.google.common.base.Stopwatch;
import ichttt.mods.firstaid.common.AABBAlignedBoundingBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
        Stopwatch stopwatch = Stopwatch.createStarted();
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
                stopwatch.stop();
                System.out.println("Inflation: " + inflation + " best slot: " + bestSlot);
                System.out.println("Took " + stopwatch.elapsed(TimeUnit.MICROSECONDS) + " us");
                return bestSlot;
            }
        }
        stopwatch.stop();
        System.out.println("Not found!");
        System.out.println("Took " + stopwatch.elapsed(TimeUnit.MICROSECONDS) + " us");
        return null;
    }

}
