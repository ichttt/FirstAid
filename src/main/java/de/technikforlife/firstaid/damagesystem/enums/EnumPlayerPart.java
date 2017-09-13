package de.technikforlife.firstaid.damagesystem.enums;

import de.technikforlife.firstaid.EventHandler;

public enum EnumPlayerPart {
    HEAD(1), LEFT_ARM(2), LEFT_LEG(3), LEFT_FOOT(4), BODY(5), RIGHT_ARM(6), RIGHT_LEG(7), RIGHT_FOOT(8);

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

    public static EnumPlayerPart getRandomPart() {
        int value = EventHandler.rand.nextInt(8);
        return fromID(value + 1);
    }
}
