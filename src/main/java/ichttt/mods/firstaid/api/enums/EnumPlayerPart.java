/*
 * FirstAid API
 * Copyright (c) 2017-2019
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package ichttt.mods.firstaid.api.enums;

import com.google.common.collect.ImmutableList;
import net.minecraft.inventory.EquipmentSlotType;

public enum EnumPlayerPart {
    HEAD(EquipmentSlotType.HEAD), LEFT_ARM(EquipmentSlotType.CHEST), LEFT_LEG(EquipmentSlotType.LEGS), LEFT_FOOT(EquipmentSlotType.FEET),
    BODY(EquipmentSlotType.CHEST), RIGHT_ARM(EquipmentSlotType.CHEST), RIGHT_LEG(EquipmentSlotType.LEGS), RIGHT_FOOT(EquipmentSlotType.FEET);

    public static final EnumPlayerPart[] VALUES = values();

    private ImmutableList<EnumPlayerPart> neighbours;
    public final EquipmentSlotType slot;

    EnumPlayerPart(EquipmentSlotType slot) {
        this.slot = slot;
    }

    public ImmutableList<EnumPlayerPart> getNeighbours() {
        if (neighbours == null) { // Need to do lazy init to avoid crashes when initializing class
            ImmutableList.Builder<EnumPlayerPart> builder = ImmutableList.builder();
            if (this != BODY && this != HEAD) //Not quite sure what I though when I did this, but I', going to leave this as-is right now
                builder.add(getUp());
            if (this != LEFT_FOOT && this != RIGHT_FOOT)
                builder.add(getDown());
            if (this.ordinal() >= BODY.ordinal())
                builder.add(getLeft());
            else
                builder.add(getRight());
            neighbours = builder.build();
        }
        return neighbours;
    }

    public EnumPlayerPart getUp() {
        if (this == BODY)
            throw new RuntimeException("There is no part up from " + this);
        return VALUES[this.ordinal() - 1];
    }

    public EnumPlayerPart getDown() {
        if (this == LEFT_FOOT)
            throw new RuntimeException("There is no part down from " + this);
        return VALUES[this.ordinal() + 1];
    }

    public EnumPlayerPart getLeft() {
        return VALUES[this.ordinal() - 4];
    }

    public EnumPlayerPart getRight() {
        return VALUES[this.ordinal() + 4];
    }

}
