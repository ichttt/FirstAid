package ichttt.mods.firstaid.damagesystem.debuff;

import ichttt.mods.firstaid.damagesystem.enums.EnumPlayerPart;

public class Debuffs {

    public static AbstractDebuff[] getHeadDebuffs() {
        AbstractDebuff[] debuffs = new AbstractDebuff[2];
        debuffs[0] = new OnHitDebuff("blindness")
                .addCondition(2F, 8 * 20)
                .addCondition(1F, 4 * 20);
        debuffs[1] = new OnHitDebuff("nausea")
                .addCondition(3F, 16 * 20)
                .addCondition(2F, 12 * 20);
        return debuffs;
    }

    public static AbstractDebuff[] getBodyDebuffs() {
        AbstractDebuff[] debuffs = new AbstractDebuff[2];
        debuffs[0] = new OnHitDebuff("nausea")
                .addCondition(4F, 16 * 20)
                .addCondition(2F, 8 * 20);
        debuffs[1] = new ConstantDebuff("weakness")
                .addBound(0.25F, 2)
                .addBound(0.50F, 1);
        return debuffs;
    }

    public static SharedDebuff getArmDebuffs() {
        return new SharedDebuff(new ConstantDebuff("mining_fatigue")
                .addBound(0.25F, 3)
                .addBound(0.50F, 2)
                .addBound(0.75F, 1)
                , EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM);
    }

    public static SharedDebuff getLegFootDebuffs() {
        return new SharedDebuff(new ConstantDebuff("slowness")
                .addBound(0.35F, 3)
                .addBound(0.6F, 2)
                .addBound(0.85F, 1)
                , EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_FOOT, EnumPlayerPart.RIGHT_LEG);
    }
}
