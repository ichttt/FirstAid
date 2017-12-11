package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.IProxy;
import ichttt.mods.firstaid.api.enums.EnumHealingType;
import ichttt.mods.firstaid.client.gui.GuiApplyHealthItem;
import ichttt.mods.firstaid.client.gui.HUDHandler;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import ichttt.mods.firstaid.api.enums.EnumHealingType;
import ichttt.mods.firstaid.items.FirstAidItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
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
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.BANDAGE, 0, new ModelResourceLocation("firstaid:bandage"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.PLASTER, 0, new ModelResourceLocation("firstaid:plaster"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.MORPHINE, 0, new ModelResourceLocation("firstaid:morphine"));
    }

    @Override
    public void showGuiApplyHealth(EnumHealingType healingType, EnumHand activeHand) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiApplyHealthItem.INSTANCE = new GuiApplyHealthItem(PlayerDataManager.getDamageModel(mc.player), healingType, activeHand);
        mc.displayGuiScreen(GuiApplyHealthItem.INSTANCE);
    }
}
