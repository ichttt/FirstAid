package de.technikforlife.firstaid.damagesystem;

public enum EnumHealingType {
    BANDAGE(1), PFLASTER(2);

    public final int id;

    EnumHealingType(int id) {
        this.id = id;
    }

    public static EnumHealingType fromID(byte b) {
        switch (b) {
            case 1:
                return BANDAGE;
            case 2:
                return PFLASTER;
            default:
                throw new IndexOutOfBoundsException("Invalid ID " + b);
        }
    }
}
