package ichttt.mods.firstaid.api.enums;

import ichttt.mods.firstaid.api.PartHealer;

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
