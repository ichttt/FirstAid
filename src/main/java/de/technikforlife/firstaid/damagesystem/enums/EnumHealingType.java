package de.technikforlife.firstaid.damagesystem.enums;

import de.technikforlife.firstaid.damagesystem.PartHealer;

public enum EnumHealingType {
    BANDAGE(1, PartHealer.BANDAGE), PLASTER(2, PartHealer.PLASTER);

    public final int id;
    public final PartHealer partHealer;

    EnumHealingType(int id, PartHealer partHealer) {
        this.id = id;
        this.partHealer = partHealer;
    }

    public static EnumHealingType fromID(byte b) {
        switch (b) {
            case 1:
                return BANDAGE;
            case 2:
                return PLASTER;
            default:
                throw new IndexOutOfBoundsException("Invalid ID " + b);
        }
    }
}
