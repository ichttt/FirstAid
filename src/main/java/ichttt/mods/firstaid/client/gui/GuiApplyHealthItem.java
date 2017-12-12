package ichttt.mods.firstaid.client.gui;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.FirstAidRegistryImpl;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumHealingType;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.ClientProxy;
import ichttt.mods.firstaid.common.network.MessageApplyHealingItem;
import ichttt.mods.firstaid.common.network.MessageClientUpdate;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumHand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class GuiApplyHealthItem extends GuiScreen {
    public static GuiApplyHealthItem INSTANCE;
    public static final int xSize = 256;
    public static final int ySize = 137;

    public int guiLeft;
    public int guiTop;

    private GuiButton HEAD, LEFT_ARM, LEFT_LEG, LEFT_FOOT, BODY, RIGHT_ARM, RIGHT_LEG, RIGHT_FOOT;

    private final AbstractPlayerDamageModel damageModel;
    private EnumHealingType healingType;
    private EnumHand activeHand;
    private final boolean disableButtons;

    public static boolean isOpen = false;

    public GuiApplyHealthItem(AbstractPlayerDamageModel damageModel) {
        this.damageModel = damageModel;

        disableButtons = true;
    }

    public GuiApplyHealthItem(AbstractPlayerDamageModel damageModel, EnumHealingType healingType, EnumHand activeHand) {
        this.damageModel = damageModel;
        this.healingType = healingType;
        this.activeHand = activeHand;

        disableButtons = false;
    }

    @Override
    public void initGui() {
        isOpen = true;
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;

        HEAD = new GuiButton(1, this.guiLeft + 4, this.guiTop + 8, 52, 20, I18n.format("gui.head"));
        this.buttonList.add(HEAD);

        LEFT_ARM = new GuiButton(2, this.guiLeft + 4, this.guiTop + 33, 52, 20, I18n.format("gui.left_arm"));
        this.buttonList.add(LEFT_ARM);
        LEFT_LEG = new GuiButton(3, this.guiLeft + 4, this.guiTop + 58, 52, 20, I18n.format("gui.left_leg"));
        this.buttonList.add(LEFT_LEG);
        LEFT_FOOT = new GuiButton(4, this.guiLeft + 4, this.guiTop + 83, 52, 20, I18n.format("gui.left_foot"));
        this.buttonList.add(LEFT_FOOT);

        BODY = new GuiButton(5, this.guiLeft + 199, this.guiTop + 8, 52, 20, I18n.format("gui.body"));
        this.buttonList.add(BODY);

        RIGHT_ARM = new GuiButton(6, this.guiLeft + 199, this.guiTop + 33, 52, 20, I18n.format("gui.right_arm"));
        this.buttonList.add(RIGHT_ARM);
        RIGHT_LEG = new GuiButton(7, this.guiLeft + 199, this.guiTop + 58, 52, 20, I18n.format("gui.right_leg"));
        this.buttonList.add(RIGHT_LEG);
        RIGHT_FOOT = new GuiButton(8, this.guiLeft + 199, this.guiTop + 83, 52, 20, I18n.format("gui.right_foot"));
        this.buttonList.add(RIGHT_FOOT);

        if (disableButtons) {
            HEAD.enabled = false;
            LEFT_ARM.enabled = false;
            LEFT_LEG.enabled = false;
            LEFT_FOOT.enabled = false;
            BODY.enabled = false;
            RIGHT_ARM.enabled = false;
            RIGHT_LEG.enabled = false;
            RIGHT_FOOT.enabled = false;
        }

        GuiButton buttonCancel = new GuiButton(9, this.width / 2 - 100, this.height - 50, I18n.format("gui.cancel"));
        this.buttonList.add(buttonCancel);

        if (this.mc.gameSettings.showDebugInfo) {
            GuiButton REFRESH = new GuiButton(10, this.guiLeft + 218, this.guiTop + 115, 36, 20, "resync");
            this.buttonList.add(REFRESH);
        }

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + xSize, this.guiTop + ySize, -16777216, -16777216);
        this.mc.getTextureManager().bindTexture(GuiUtils.GUI_LOCATION);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
        GuiInventory.drawEntityOnScreen(this.width / 2, this.height / 2 + 30, 45, 0, 0, mc.player);

        super.drawScreen(mouseX, mouseY, partialTicks);

        int morphineTicks = damageModel.getMorphineTicks();
        if (morphineTicks > 0)
            drawCenteredString(this.mc.fontRenderer, I18n.format("gui.morphine_left", StringUtils.ticksToElapsedTime(morphineTicks)), this.guiLeft + (xSize / 2), this.guiTop + ySize - 29, 0xFFFFFF);
        drawCenteredString(this.mc.fontRenderer, I18n.format("gui.apply_hint"), this.guiLeft + (xSize / 2), this.guiTop + ySize - (morphineTicks == 0 ? 21 : 11), 0xFFFFFF);

        this.mc.getTextureManager().bindTexture(Gui.ICONS);
        boolean playerDead = damageModel.isDead(mc.player);
        drawHealth(damageModel.HEAD, false, 14, playerDead);
        drawHealth(damageModel.LEFT_ARM, false, 39, playerDead);
        drawHealth(damageModel.LEFT_LEG, false, 64, playerDead);
        drawHealth(damageModel.LEFT_FOOT, false, 89, playerDead);
        drawHealth(damageModel.BODY, true, 14, playerDead);
        drawHealth(damageModel.RIGHT_ARM, true, 39, playerDead);
        drawHealth(damageModel.RIGHT_LEG, true, 64, playerDead);
        drawHealth(damageModel.RIGHT_FOOT, true, 89, playerDead);

        GlStateManager.pushMatrix();
        tooltipButton(HEAD, damageModel.HEAD, mouseX, mouseY);
        tooltipButton(LEFT_ARM, damageModel.LEFT_ARM, mouseX, mouseY);
        tooltipButton(LEFT_LEG, damageModel.LEFT_LEG, mouseX, mouseY);
        tooltipButton(LEFT_FOOT, damageModel.LEFT_FOOT, mouseX, mouseY);
        tooltipButton(BODY, damageModel.BODY, mouseX, mouseY);
        tooltipButton(RIGHT_ARM, damageModel.RIGHT_ARM, mouseX, mouseY);
        tooltipButton(RIGHT_LEG, damageModel.RIGHT_LEG, mouseX, mouseY);
        tooltipButton(RIGHT_FOOT, damageModel.RIGHT_FOOT, mouseX, mouseY);
        GlStateManager.popMatrix();
        GlStateManager.disableLighting();
        //TODO color the critical parts of the player red?
    }

    private void tooltipButton(GuiButton button, AbstractDamageablePart part, int mouseX, int mouseY) {
        boolean enabled = part.activeHealer == null;
        if (!enabled && button.hovered)
            drawHoveringText(I18n.format("gui.active_item") + ": " + I18n.format("item." + part.activeHealer.healingType.toString().toLowerCase(Locale.ENGLISH) + ".name"), mouseX, mouseY);
        if (!disableButtons)
            button.enabled = enabled;
    }

    public void drawHealth(AbstractDamageablePart damageablePart, boolean right, int yOffset, boolean playerDead) {
        GuiUtils.drawHealth(damageablePart, guiLeft + (right ? 200 - Math.min(38, GuiUtils.getMaxHearts(damageablePart.getMaxHealth()) * 9 + GuiUtils.getMaxHearts(damageablePart.getAbsorption()) * 9 + 2) : 57), guiTop + yOffset, this, true, playerDead);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == ClientProxy.showWounds.getKeyCode())
            mc.displayGuiScreen(null);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id < 9) {
            EnumPlayerPart playerPart = EnumPlayerPart.fromID((button.id));
            FirstAid.NETWORKING.sendToServer(new MessageApplyHealingItem(playerPart, healingType, activeHand));
            //TODO notify the user somehow (sound?)
            AbstractDamageablePart part = damageModel.getFromEnum(playerPart);
            part.activeHealer = FirstAidRegistryImpl.INSTANCE.getPartHealer(healingType);
        } else if (button.id == 10) {
            FirstAid.NETWORKING.sendToServer(new MessageClientUpdate(MessageClientUpdate.Type.REQUEST_REFRESH));
            FirstAid.logger.info("Requesting refresh");
            mc.player.sendStatusMessage(new TextComponentString("Re-downloading health data from server..."), true);
        }
        mc.displayGuiScreen(null);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        INSTANCE = null;
        isOpen = false;
        super.onGuiClosed();
    }

    public List<GuiButton> getButtons() {
        return buttonList;
    }
}
