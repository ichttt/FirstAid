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

package ichttt.mods.firstaid.common;

import net.minecraft.world.phys.AABB;

public class AABBAlignedBoundingBox {
    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;

    public AABBAlignedBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public AABB createAABB(AABB original) {
        double sizeX = original.getXsize();
        double sizeY = original.getYsize();
        double sizeZ = original.getZsize();
        double newMinX = original.minX + (sizeX * minX);
        double newMinY = original.minY + (sizeY * minY);
        double newMinZ = original.minZ + (sizeZ * minZ);
        double newMaxX = original.minX + (sizeX * maxX);
        double newMaxY = original.minY + (sizeY * maxY);
        double newMaxZ = original.minZ + (sizeZ * maxZ);
        return new AABB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }
}
