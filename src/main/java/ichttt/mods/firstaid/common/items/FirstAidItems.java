package ichttt.mods.firstaid.common.items;

public class FirstAidItems {
    public static ItemHealing BANDAGE;
    public static ItemHealing PLASTER;
    public static ItemMorphine MORPHINE;

    public static void init() {
        BANDAGE = new ItemHealing("bandage");
        PLASTER = new ItemHealing("plaster");
        MORPHINE = new ItemMorphine();
    }
}
