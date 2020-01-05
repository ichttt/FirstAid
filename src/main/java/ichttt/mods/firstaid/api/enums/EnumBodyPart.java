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
import net.minecraft.inventory.EntityEquipmentSlot;

public enum EnumBodyPart {
    HEAD(1, EntityEquipmentSlot.HEAD), LEFT_ARM(2, EntityEquipmentSlot.CHEST), LEFT_LEG(3, EntityEquipmentSlot.LEGS), LEFT_FOOT(4, EntityEquipmentSlot.FEET),
    BODY(5, EntityEquipmentSlot.CHEST), RIGHT_ARM(6, EntityEquipmentSlot.CHEST), RIGHT_LEG(7, EntityEquipmentSlot.LEGS), RIGHT_FOOT(8, EntityEquipmentSlot.FEET);

    public static final EnumBodyPart[] VALUES = values();

    public final byte id;
    private ImmutableList<EnumBodyPart> neighbours;
    public final EntityEquipmentSlot slot;

    EnumBodyPart(int id, EntityEquipmentSlot slot) {
        this.id = (byte) id;
        this.slot = slot;
    }

    public ImmutableList<EnumBodyPart> getNeighbours() {
        if (neighbours == null) { // Need to do lazy init to avoid crashes when initializing class
            ImmutableList.Builder<EnumBodyPart> builder = ImmutableList.builder();
            if (this.id != 5 && this.id != 1)
                builder.add(getUp());
            if (this.id != 4 && this.id != 8)
                builder.add(getDown());
            if (this.id > 4)
                builder.add(getLeft());
            else
                builder.add(getRight());
            neighbours = builder.build();
        }
        return neighbours;
    }

    public static EnumBodyPart fromID(int id) {
        switch (id) {
            case 1:
                return HEAD;
            case 2:
                return LEFT_ARM;
            case 3:
                return LEFT_LEG;
            case 4:
                return LEFT_FOOT;
            case 5:
                return BODY;
            case 6:
                return RIGHT_ARM;
            case 7:
                return RIGHT_LEG;
            case 8:
                return RIGHT_FOOT;
        }
        throw new IndexOutOfBoundsException("Invalid id " + id);
    }

    public EnumBodyPart getUp() {
        if (this.id == 5)
            throw new IndexOutOfBoundsException("There is no part up from " + this.id);
        return fromID(this.id - 1);
    }

    public EnumBodyPart getDown() {
        if (this.id == 4)
            throw new IndexOutOfBoundsException("There is no part down from " + this.id);
        return fromID(this.id + 1);
    }

    public EnumBodyPart getLeft() {
        return fromID(this.id - 4);
    }

    public EnumBodyPart getRight() {
        return fromID(this.id + 4);
    }

}
