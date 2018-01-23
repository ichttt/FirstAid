package ichttt.mods.firstaid.common.items;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class FirstAidItems {
    public static ItemHealing BANDAGE;
    public static ItemHealing PLASTER;
    public static ItemMorphine MORPHINE;

    public static void init() {
        BANDAGE = new ItemHealing("bandage");
        PLASTER = new ItemHealing("plaster");
        MORPHINE = new ItemMorphine();
        GameRegistry.register(BANDAGE);
        GameRegistry.register(PLASTER);
        GameRegistry.register(MORPHINE);
    }
}
