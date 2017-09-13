package de.technikforlife.firstaid.client;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.DamageablePart;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.enums.EnumHealingType;
import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import de.technikforlife.firstaid.network.MessageApplyHealth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiApplyHealthItem extends GuiScreen {
    public static GuiApplyHealthItem INSTANCE;
    public static final ResourceLocation GUI_LOCATION = new ResourceLocation(FirstAid.MODID, "textures/gui/show_wounds.png");
    public static final int xSize = 248;
    public static final int ySize = 137;

    public int guiLeft;
    public int guiTop;

    private GuiButton HEAD, LEFT_ARM, LEFT_LEG, LEFT_FOOT, BODY, RIGHT_ARM, RIGHT_LEG, RIGHT_FOOT;

    public PlayerDamageModel damageModel;
    private EnumHealingType healingType;
    private EnumHand activeHand;
    public boolean hasData = false;
    private boolean disableButtons = false;

    public static boolean isOpen = false;

    public void onReceiveData(PlayerDamageModel damageModel) {
        this.damageModel = damageModel;
        hasData = true;

        disableButtons = true;

        addMainButtons();
    }

    public void onReceiveData(PlayerDamageModel damageModel, EnumHealingType healingType, EnumHand activeHand) {
        this.damageModel = damageModel;
        this.healingType = healingType;
        this.activeHand = activeHand;

        addMainButtons();

        hasData = true;
        disableButtons = false;
    }

    private void addMainButtons() {
        HEAD = new GuiButton(1, this.guiLeft + 4, this.guiTop + 8, 48, 20, I18n.format("gui.head"));
        this.buttonList.add(HEAD);

        LEFT_ARM = new GuiButton(2, this.guiLeft + 4, this.guiTop + 33, 48, 20, I18n.format("gui.left_arm"));
        this.buttonList.add(LEFT_ARM);
        LEFT_LEG = new GuiButton(3, this.guiLeft + 4, this.guiTop + 58, 48, 20, I18n.format("gui.left_leg"));
        this.buttonList.add(LEFT_LEG);
        LEFT_FOOT = new GuiButton(4, this.guiLeft + 4, this.guiTop + 83, 48, 20, I18n.format("gui.left_foot"));
        this.buttonList.add(LEFT_FOOT);

        BODY = new GuiButton(5, this.guiLeft + 195, this.guiTop + 8, 48, 20, I18n.format("gui.body"));
        this.buttonList.add(BODY);

        RIGHT_ARM = new GuiButton(6, this.guiLeft + 195, this.guiTop + 33, 48, 20, I18n.format("gui.right_arm"));
        this.buttonList.add(RIGHT_ARM);
        RIGHT_LEG = new GuiButton(7, this.guiLeft + 195, this.guiTop + 58, 48, 20, I18n.format("gui.right_leg"));
        this.buttonList.add(RIGHT_LEG);
        RIGHT_FOOT = new GuiButton(8, this.guiLeft + 195, this.guiTop + 83, 48, 20, I18n.format("gui.right_foot"));
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
    }

    @Override
    public void initGui() {
        isOpen = true;
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;
        if (hasData) {
            this.buttonList.clear();
            addMainButtons();
        }
        GuiButton buttonCancel = new GuiButton(9, this.width / 2 - 100, this.height - 50, I18n.format("gui.cancel"));
        this.buttonList.add(buttonCancel);
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + xSize, this.guiTop + ySize, -16777216, -16777216);
        this.mc.getTextureManager().bindTexture(GUI_LOCATION);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
        GuiInventory.drawEntityOnScreen(this.width / 2, this.height / 2 + 30, 45, 0, 0, mc.player);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (hasData) {
            int morphineSecs = Math.round(damageModel.getMorphineTicks() / 20F);
            if (morphineSecs > 0)
                drawCenteredString(this.mc.fontRenderer, I18n.format("gui.morphine_left", morphineSecs), this.guiLeft + (xSize / 2), this.guiTop + ySize - 29, 0xFFFFFF);
            drawCenteredString(this.mc.fontRenderer, I18n.format("gui.apply_hint"), this.guiLeft + (xSize / 2), this.guiTop + ySize - (morphineSecs == 0 ? 21 : 11), 0xFFFFFF);

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

            this.mc.getTextureManager().bindTexture(Gui.ICONS);
            drawHealth(damageModel.HEAD, false, 14);
            drawHealth(damageModel.LEFT_ARM, false, 39);
            drawHealth(damageModel.LEFT_LEG, false, 64);
            drawHealth(damageModel.LEFT_FOOT, false, 89);
            drawHealth(damageModel.BODY, true, 14);
            drawHealth(damageModel.RIGHT_ARM, true, 39);
            drawHealth(damageModel.RIGHT_LEG, true, 64);
            drawHealth(damageModel.RIGHT_FOOT, true, 89);
            //TODO color the critical parts of the player red?
        } else {
            drawCenteredString(this.mc.fontRenderer, "Waiting for data...", this.guiLeft + (xSize / 2), this.guiTop + ySize - 21, 0xFFFFFF);
        }
    }

    private void tooltipButton(GuiButton button, DamageablePart part, int mouseX, int mouseY) {
        boolean enabled = part.activeHealer == null;
        if (!enabled && button.hovered) {
            drawHoveringText("Currently active: " + part.activeHealer.healingType, mouseX, mouseY);
        }
        if (!disableButtons)
            button.enabled = enabled;
    }

    private void drawHealth(DamageablePart damageablePart, boolean right, int yOffset) {
        GlStateManager.pushMatrix();
        int maxHealth = Math.round(damageablePart.maxHealth);
        GlStateManager.translate(guiLeft + (right ? 194 - (Math.min(4F, maxHealth / 2F) * 9F) : 53), guiTop + yOffset, 0);
        int yTexture = damageablePart.canCauseDeath ? 45 : 0;
        renderIcon(damageablePart.maxHealth, damageablePart.maxHealth, yTexture, 16, 16);
        renderIcon(damageablePart.maxHealth, damageablePart.currentHealth, yTexture, 52, 61);
        GlStateManager.popMatrix();
    }

    private void renderIcon(float max, float available, int textureY, int textureX, int halfTextureX) {
        GlStateManager.pushMatrix();
        int maxHealth = Math.round(max);
        int availableHealth = Math.round(available);
        boolean lastOneHalf = availableHealth % 2 != 0;
        if (maxHealth > 16)
            throw new IllegalArgumentException("Can only draw up to 8 hearts!");
        int toDraw = Math.min(4, Math.round(availableHealth / 2F));
        if (maxHealth > 8) {
            GlStateManager.translate(0, 5, 0);
            int toDrawSecond = (int) ((availableHealth - 8) / 2F) + (lastOneHalf ? 1 : 0);
            if (toDrawSecond > 0 && availableHealth > 8)
                renderTexturedModalRects(toDrawSecond, lastOneHalf, halfTextureX, textureX, textureY);
            GlStateManager.translate(0, -10, 0);
        }
        renderTexturedModalRects(toDraw, lastOneHalf && availableHealth < 8, halfTextureX, textureX, textureY);
        GlStateManager.popMatrix();
    }

    private void renderTexturedModalRects(int toDraw, boolean lastOneHalf, int halfTextureX, int textureX, int textureY) {
        for (int i = 0; i < toDraw; i++) {
            boolean renderHalf = lastOneHalf && i + 1 == toDraw;
            int width = (renderHalf && halfTextureX == textureX) ? 5 : 9;
            drawTexturedModalRect(i * 9, 0, renderHalf ? halfTextureX : textureX, textureY, width, 9);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id < 9 && hasData) {
            EnumPlayerPart playerPart = EnumPlayerPart.fromID((button.id));
            FirstAid.NETWORKING.sendToServer(new MessageApplyHealth(playerPart, healingType, activeHand));
            //TODO notify the user somehow (sound?)
        }
        Minecraft.getMinecraft().displayGuiScreen(null);
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
