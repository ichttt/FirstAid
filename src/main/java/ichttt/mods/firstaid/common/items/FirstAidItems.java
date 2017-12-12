package ichttt.mods.firstaid.common.items;

import ichttt.mods.firstaid.api.enums.EnumHealingType;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FirstAidItems {
    public static ItemHealing BANDAGE;
    public static ItemHealing PLASTER;
    public static ItemMorphine MORPHINE;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(BANDAGE, PLASTER, MORPHINE);
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(FirstAidItems.class);
        BANDAGE = new ItemHealing("bandage", EnumHealingType.BANDAGE);
        PLASTER = new ItemHealing("plaster", EnumHealingType.PLASTER);
        MORPHINE = new ItemMorphine();
    }
}
