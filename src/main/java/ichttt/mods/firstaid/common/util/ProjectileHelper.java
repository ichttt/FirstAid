/*
 * FirstAid
 * Copyright (C) 2017-2020
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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public class ProjectileHelper {
    //PLAYER: 0, 0, 0, 0.6, 1.6, 0.6
    private static final AxisAlignedBB HEAD_AABB = new AxisAlignedBB(0D, 1.5D, 0D, 1D, 2.0D, 1D);
    private static final AxisAlignedBB MAIN_AABB = new AxisAlignedBB(0D, 0.8D, 0D, 1D, 1.5D, 1D);
    private static final AxisAlignedBB LEG_AABB = new AxisAlignedBB(0D, 0.4D, 0D, 1D, 0.8D, 1D);
    private static final AxisAlignedBB FEET_AABB = new AxisAlignedBB(0D, 0.0D, 0D, 1D, 0.4D, 1D);

    @Nullable
    public static EntityEquipmentSlot getPartByPosition(Entity hittingObject, EntityPlayer toTrack) {
        if (testAABB(hittingObject, toTrack, HEAD_AABB))
            return EntityEquipmentSlot.HEAD;
        if (testAABB(hittingObject, toTrack, MAIN_AABB))
            return EntityEquipmentSlot.CHEST;
        if (testAABB(hittingObject, toTrack, LEG_AABB))
            return EntityEquipmentSlot.LEGS;
        if (testAABB(hittingObject, toTrack, FEET_AABB))
            return EntityEquipmentSlot.FEET;
        return null;
    }

    private static boolean testAABB(Entity hittingObject, EntityPlayer toTest, AxisAlignedBB aabb) {
        AxisAlignedBB toTestAABB = hittingObject.getEntityBoundingBox();
        return (toTestAABB.minY - toTest.posY) < aabb.maxY && (toTestAABB.maxY - toTest.posY) > aabb.minY;
    }
}
