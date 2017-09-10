package de.technikforlife.firstaid.client;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.IProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.KeyBinding;
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
    }

    @Override
    public void showGuiApplyHealth() {
        GuiApplyHealthItem.INSTANCE = new GuiApplyHealthItem();
        Minecraft.getMinecraft().displayGuiScreen(GuiApplyHealthItem.INSTANCE);
    }

    @Override
    public void healClient(float amount) {
        if (GuiApplyHealthItem.INSTANCE != null && GuiApplyHealthItem.isOpen) {
            GuiApplyHealthItem.INSTANCE.damageModel.forEach(part -> part.heal(amount));
        }
    }
}
