package de.technikforlife.firstaid.items;

import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FirstAidItems {
    public static ItemHealing BANDAGE;
    public static ItemHealing PLASTER;

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(BANDAGE, PLASTER);
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new FirstAidItems());
        BANDAGE = new ItemHealing("bandage", EnumHealingType.BANDAGE);
        PLASTER = new ItemHealing("plaster", EnumHealingType.PLASTER);
    }
}
