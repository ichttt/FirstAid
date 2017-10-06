package ichttt.mods.firstaid.damagesystem.enums;

import ichttt.mods.firstaid.damagesystem.PartHealer;

public enum EnumHealingType {
    BANDAGE(1) {
        @Override
        public PartHealer createNewHealer() {
            return new PartHealer(400, 3, this);
        }
    }, PLASTER(2) {
        @Override
        public PartHealer createNewHealer() {
            return new PartHealer(500, 2, this);
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
