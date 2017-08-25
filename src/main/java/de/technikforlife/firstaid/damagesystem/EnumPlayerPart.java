package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.damagesystem.capability.DamageEventHandler;

public enum EnumPlayerPart {
    HEAD(1), LEFT_ARM(2), LEFT_LEG(3), BODY(4), RIGHT_ARM(5), RIGHT_LEG(6);

    public final byte id;
    EnumPlayerPart(int id) {

        this.id = (byte) id;
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
                return BODY;
            case 5:
                return RIGHT_ARM;
            case 6:
                return RIGHT_LEG;
        }
        throw new IndexOutOfBoundsException("Invalid id " + id);
    }

    public static EnumPlayerPart getRandomPart() {
        int value = DamageEventHandler.rand.nextInt(6);
        return fromID(value + 1);
    }
}
