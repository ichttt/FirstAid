package de.technikforlife.firstaid.client;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.IProxy;
import de.technikforlife.firstaid.client.gui.GuiApplyHealthItem;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.damagesystem.distribution.HealthDistribution;
import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Objects;

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
        GuiApplyHealthItem.INSTANCE = new GuiApplyHealthItem(Minecraft.getMinecraft().player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null), healingType, activeHand);
        Minecraft.getMinecraft().displayGuiScreen(GuiApplyHealthItem.INSTANCE);
    }

    @Override
    public void healClient(float amount) {
        HealthDistribution.distributeHealth(amount, Minecraft.getMinecraft().player);
    }
}
