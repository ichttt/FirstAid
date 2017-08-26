package de.technikforlife.firstaid.damagesystem.enums;

import de.technikforlife.firstaid.damagesystem.PartHealer;

public enum EnumHealingType {
    BANDAGE(1) {
        @Override
        public PartHealer createNewHealer() {
            return PartHealer.getNewBandage();
        }
    }, PLASTER(2) {
        @Override
        public PartHealer createNewHealer() {
            return PartHealer.getNewPlaster();
        }
    };

    public final int id;

    EnumHealingType(int id) {
        this.id = id;
    }

    public abstract PartHealer createNewHealer();

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
