package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.IProxy;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ClientProxy implements IProxy {
    public static final KeyBinding showWounds = new KeyBinding("keybinds.show_wounds", KeyConflictContext.IN_GAME, Keyboard.KEY_H, FirstAid.NAME);

    @Override
    public void init() {
        FirstAid.logger.debug("Loading ClientProxy");
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        ClientRegistry.registerKeyBinding(showWounds);
        GuiIngameForge.renderHealth = FirstAidConfig.overlay.showVanillaHealthBar;
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(manager -> HUDHandler.rebuildTranslationTable());
        EventCalendar.checkDate();
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.BANDAGE, 0, new ModelResourceLocation("firstaid:bandage"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.PLASTER, 0, new ModelResourceLocation("firstaid:plaster"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.MORPHINE, 0, new ModelResourceLocation("firstaid:morphine"));
    }

    @Override
    public void showGuiApplyHealth(EnumHand activeHand) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiHealthScreen.INSTANCE = new GuiHealthScreen(PlayerDataManager.getDamageModel(mc.player), activeHand);
        mc.displayGuiScreen(GuiHealthScreen.INSTANCE);
    }
}
