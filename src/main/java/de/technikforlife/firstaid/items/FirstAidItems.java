package de.technikforlife.firstaid.items;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FirstAidItems {
    public static ItemBandage BANDAGE;

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(BANDAGE);
    }
    public static void init() {
        MinecraftForge.EVENT_BUS.register(new FirstAidItems());
        BANDAGE = new ItemBandage();
    }
}
