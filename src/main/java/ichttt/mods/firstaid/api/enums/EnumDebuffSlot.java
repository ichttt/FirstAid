package ichttt.mods.firstaid.api.enums;

import javax.annotation.Nonnull;

public enum EnumDebuffSlot {
    HEAD(EnumPlayerPart.HEAD), BODY(EnumPlayerPart.BODY),
    ARMS(EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM),
    LEGS_AND_FEET(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT);

    EnumDebuffSlot(@Nonnull EnumPlayerPart... playerParts) {
        this.playerParts = playerParts;
    }

    @Nonnull
    public final EnumPlayerPart[] playerParts;
}
