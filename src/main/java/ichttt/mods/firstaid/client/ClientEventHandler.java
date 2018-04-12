package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.tutorial.GuiTutorial;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.apiimpl.RegistryManager;
import ichttt.mods.firstaid.common.config.ConfigEntry;
import ichttt.mods.firstaid.common.config.ExtraConfig;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.common.items.FirstAidItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {
    private static int id;

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.BANDAGE, 0, new ModelResourceLocation("firstaid:bandage"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.PLASTER, 0, new ModelResourceLocation("firstaid:plaster"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.MORPHINE, 0, new ModelResourceLocation("firstaid:morphine"));
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || mc.player.connection == null || mc.isGamePaused()) return;
        if (EventCalendar.isGuiFun()) {
            GuiHealthScreen.BED_ITEMSTACK.setItemDamage(id);
            if (mc.world != null && mc.world.getWorldTime() % 3 == 0) id++;
            if (id > 15) id = 0;
        }
        if (!RegistryManager.debuffConfigErrors.isEmpty() && mc.world != null && mc.world.isRemote && mc.player != null) {
            mc.player.sendStatusMessage(new TextComponentString("[FirstAid] FirstAid has detected invalid debuff config entries."), false);
            for (String s : RegistryManager.debuffConfigErrors)
                mc.player.sendStatusMessage(new TextComponentString("[FirstAid] " + s), false);
            RegistryManager.debuffConfigErrors.clear();
        }
        if (HUDHandler.ticker > 0)
            HUDHandler.ticker--;
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        AbstractPlayerDamageModel damageModel = PlayerDataManager.getDamageModel(mc.player);
        if (ClientProxy.showWounds.isPressed()) {
            if (!damageModel.hasTutorial) {
                damageModel.hasTutorial = true;
                PlayerDataManager.tutorialDone.add(mc.player.getName());
                Minecraft.getMinecraft().displayGuiScreen(new GuiTutorial());
            }
            else {
                mc.displayGuiScreen(new GuiHealthScreen(damageModel));
            }
        }
    }

    @SubscribeEvent
    public static void preRender(RenderGameOverlayEvent.Pre event) {
        RenderGameOverlayEvent.ElementType type = event.getType();
        if (type == RenderGameOverlayEvent.ElementType.HEALTH && !FirstAidConfig.overlay.showVanillaHealthBar) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent.Post event) {
        RenderGameOverlayEvent.ElementType type = event.getType();
        if (type == RenderGameOverlayEvent.ElementType.ALL || (type == RenderGameOverlayEvent.ElementType.TEXT && FirstAidConfig.overlay.position == 2)) {
            GuiIngameForge.renderHealth = FirstAidConfig.overlay.showVanillaHealthBar;
            HUDHandler.renderOverlay(event.getResolution(), event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public static void tooltipItems(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == FirstAidItems.MORPHINE) {
            event.getToolTip().add(I18n.format("firstaid.tooltip.morphine", "3:30-4:30"));
            return;
        }

        AbstractPartHealer healer = FirstAidRegistryImpl.INSTANCE.getPartHealer(stack);
        if (healer != null) {
            event.getToolTip().add(I18n.format("firstaid.tooltip.healer", healer.maxHeal, StringUtils.ticksToElapsedTime(healer.ticksPerHeal)));
        }
    }

    @SubscribeEvent
    public static void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        FirstAid.isSynced = false;
        for (ConfigEntry<ExtraConfig.Sync> option : FirstAid.syncedConfigOptions) {
            if (option.hasRemoteData())
                option.revert();
            else
                FirstAid.logger.error("Missing remote data for field " + option.field + ", cannot revert!");
        }
        HUDHandler.ticker = -1;
    }
}
