package de.technikforlife.firstaid.damagesystem.enums;

import com.google.common.collect.ImmutableList;
import net.minecraft.inventory.EntityEquipmentSlot;

public enum EnumPlayerPart {
    HEAD(1, EntityEquipmentSlot.HEAD), LEFT_ARM(2, EntityEquipmentSlot.CHEST), LEFT_LEG(3, EntityEquipmentSlot.LEGS), LEFT_FOOT(4, EntityEquipmentSlot.FEET),
    BODY(5, EntityEquipmentSlot.CHEST), RIGHT_ARM(6, EntityEquipmentSlot.CHEST), RIGHT_LEG(7, EntityEquipmentSlot.LEGS), RIGHT_FOOT(8, EntityEquipmentSlot.FEET);

    public final byte id;
    private ImmutableList<EnumPlayerPart> neighbours;
    public final EntityEquipmentSlot slot;

    EnumPlayerPart(int id, EntityEquipmentSlot slot) {
        this.id = (byte) id;
        this.slot = slot;
    }

    public ImmutableList<EnumPlayerPart> getNeighbours() {
        if (neighbours == null) { // Need to do lazy init to avoid crashes when initializing class
            ImmutableList.Builder<EnumPlayerPart> builder = ImmutableList.builder();
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

    public static EnumPlayerPart fromID(int id) {
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

    public EnumPlayerPart getUp() {
        if (this.id == 5)
            throw new IndexOutOfBoundsException("There is no part up from " + this.id);
        return fromID(this.id - 1);
    }

    public EnumPlayerPart getDown() {
        if (this.id == 4)
            throw new IndexOutOfBoundsException("There is no part down from " + this.id);
        return fromID(this.id + 1);
    }

    public EnumPlayerPart getLeft() {
        return fromID(this.id - 4);
    }

    public EnumPlayerPart getRight() {
        return fromID(this.id + 4);
    }

}
