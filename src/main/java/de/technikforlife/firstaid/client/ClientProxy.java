package de.technikforlife.firstaid.client;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.IProxy;
import de.technikforlife.firstaid.items.FirstAidItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ClientProxy implements IProxy {

    @Override
    public void init() {
        FirstAid.logger.debug("Loading ClientProxy");
        MinecraftForge.EVENT_BUS.register(ClientProxy.class);
    }

    @Override
    public void showGuiApplyHealth() {
        GuiApplyHealthItem.INSTANCE = new GuiApplyHealthItem();
        Minecraft.getMinecraft().displayGuiScreen(GuiApplyHealthItem.INSTANCE);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.BANDAGE, 0, new ModelResourceLocation("firstaid:bandage"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.PLASTER, 0, new ModelResourceLocation("firstaid:plaster"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.MORPHINE, 0, new ModelResourceLocation("firstaid:morphine"));
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entityLiving = event.getEntityLiving();
        if (entityLiving instanceof EntityPlayer) {
            if (entityLiving.getName().equals(Minecraft.getMinecraft().player.getName()) && GuiApplyHealthItem.isOpen)
                Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT && GuiApplyHealthItem.INSTANCE != null && GuiApplyHealthItem.INSTANCE.hasData && !event.player.isCreative())
            GuiApplyHealthItem.INSTANCE.damageModel.tick(event.player.world, event.player, true);
    }
}
