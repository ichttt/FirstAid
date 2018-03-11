package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.IProxy;
import ichttt.mods.firstaid.common.config.ConfigEntry;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import ichttt.mods.firstaid.common.config.ExtraConfigManager;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ClientProxy implements IProxy {
    public static final KeyBinding showWounds = new KeyBinding("keybinds.show_wounds", KeyConflictContext.IN_GAME, Keyboard.KEY_H, FirstAid.NAME);
    public static List<ConfigEntry<ExtraConfig.Advanced>> advancedConfigOptions;

    @Override
    public void preInit() {
        FirstAid.logger.debug("Loading ClientProxy");
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        ClientRegistry.registerKeyBinding(showWounds);
    }

    @Override
    public void init() {
        GuiIngameForge.renderHealth = FirstAidConfig.overlay.showVanillaHealthBar;
        EventCalendar.checkDate();
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(manager -> HUDHandler.rebuildTranslationTable());
        advancedConfigOptions = ExtraConfigManager.getAnnotatedFields(ExtraConfig.Advanced.class, FirstAidConfig.class);
    }

    @Override
    public void showGuiApplyHealth(EnumHand activeHand) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiHealthScreen.INSTANCE = new GuiHealthScreen(PlayerDataManager.getDamageModel(mc.player), activeHand);
        mc.displayGuiScreen(GuiHealthScreen.INSTANCE);
    }
}
