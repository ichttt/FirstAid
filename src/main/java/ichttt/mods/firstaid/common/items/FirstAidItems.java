package ichttt.mods.firstaid.common.items;

import ichttt.mods.firstaid.api.enums.EnumHealingType;

public class FirstAidItems {
    public static ItemHealing BANDAGE;
    public static ItemHealing PLASTER;
    public static ItemMorphine MORPHINE;

    public static void init() {
        BANDAGE = new ItemHealing("bandage", EnumHealingType.BANDAGE);
        PLASTER = new ItemHealing("plaster", EnumHealingType.PLASTER);
        MORPHINE = new ItemMorphine();
    }
}
