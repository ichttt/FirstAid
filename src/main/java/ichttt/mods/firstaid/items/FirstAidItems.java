package ichttt.mods.firstaid.items;

import ichttt.mods.firstaid.api.enums.EnumHealingType;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class FirstAidItems {
    public static ItemHealing BANDAGE;
    public static ItemHealing PLASTER;
    public static ItemMorphine MORPHINE;

    public static void init() {
        BANDAGE = new ItemHealing("bandage", EnumHealingType.BANDAGE);
        PLASTER = new ItemHealing("plaster", EnumHealingType.PLASTER);
        MORPHINE = new ItemMorphine();
        GameRegistry.register(BANDAGE);
        GameRegistry.register(PLASTER);
        GameRegistry.register(MORPHINE);
    }
}
