package de.technikforlife.firstaid.client;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.IProxy;
import de.technikforlife.firstaid.items.FirstAidItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy implements IProxy {

    @Override
    public void init() {
        FirstAid.logger.debug("Loading ClientProxy");
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.BANDAGE, 0, new ModelResourceLocation("firstaid:bandage"));
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
    }
}
