package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.IProxy;
import ichttt.mods.firstaid.client.gui.GuiApplyHealthItem;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.damagesystem.enums.EnumHealingType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.GuiIngameForge;
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
        GuiIngameForge.renderHealth = false;
    }

    @Override
    public void showGuiApplyHealth(EnumHealingType healingType, EnumHand activeHand) {
        GuiApplyHealthItem.INSTANCE = new GuiApplyHealthItem(PlayerDataManager.getDamageModel(Minecraft.getMinecraft().player), healingType, activeHand);
        Minecraft.getMinecraft().displayGuiScreen(GuiApplyHealthItem.INSTANCE);
    }

    @Override
    public void healClient(float amount) {
        EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
        if (playerSP != null)
            HealthDistribution.distributeHealth(amount, playerSP);
    }
}
