package ichttt.mods.firstaid.damagesystem.enums;

import ichttt.mods.firstaid.damagesystem.PartHealer;

public enum EnumHealingType {
    BANDAGE() {
        @Override
        public PartHealer createNewHealer() {
            return new PartHealer(400, 3, this);
        }
    }, PLASTER() {
        @Override
        public PartHealer createNewHealer() {
            return new PartHealer(500, 2, this);
        }
    };

    public abstract PartHealer createNewHealer();

    public static final EnumHealingType[] VALUES = values();
}
