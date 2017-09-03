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

@SideOnly(Side.CLIENT)
public class GuiApplyHealthItem extends GuiScreen {
    public static GuiApplyHealthItem INSTANCE;
    private static final ResourceLocation GUI_BACKGROUND = new ResourceLocation(FirstAid.MODID, "textures/gui/show_wounds.png");
    private static final int xSize = 248;
    private static final int ySize = 132;

    private int guiLeft;
    private int guiTop;

    public PlayerDamageModel damageModel;
    private EnumHealingType healingType;
    private EnumHand activeHand;
    public boolean hasData = false;

    public static boolean isOpen = false;

    public void onReceiveData(PlayerDamageModel damageModel, EnumHealingType healingType, EnumHand activeHand) {
        this.damageModel = damageModel;
        this.healingType = healingType;
        this.activeHand = activeHand;

        addMainButtons();

        hasData = true;
    }

    private void addMainButtons() {
        GuiButton applyHead = new GuiButton(1, this.guiLeft + 4, this.guiTop + 14, 48, 20, I18n.format("gui.head"));
        this.buttonList.add(applyHead);

        GuiButton applyLeftArm = new GuiButton(2, this.guiLeft + 4, this.guiTop + 44, 48, 20, I18n.format("gui.left_arm"));
        this.buttonList.add(applyLeftArm);
        GuiButton applyLeftLeg = new GuiButton(3, this.guiLeft + 4, this.guiTop + 74, 48, 20, I18n.format("gui.left_leg"));
        this.buttonList.add(applyLeftLeg);

        GuiButton applyBody = new GuiButton(4, this.guiLeft + 195, this.guiTop + 14, 48, 20, I18n.format("gui.body"));
        this.buttonList.add(applyBody);

        GuiButton applyRightArm = new GuiButton(5, this.guiLeft + 195, this.guiTop + 44, 48, 20, I18n.format("gui.right_arm"));
        this.buttonList.add(applyRightArm);
        GuiButton applyRightLeg = new GuiButton(6, this.guiLeft + 195, this.guiTop + 74, 48, 20, I18n.format("gui.right_leg"));
        this.buttonList.add(applyRightLeg);
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
        GuiButton buttonCancel = new GuiButton(7, this.width / 2 - 100, this.height - 50, I18n.format("gui.cancel"));
        this.buttonList.add(buttonCancel);
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
//        GlStateManager.colorMask(true, false, false, true);
        this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + xSize, this.guiTop + ySize, -16777216, -16777216);
        this.mc.getTextureManager().bindTexture(GUI_BACKGROUND);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
        GuiInventory.drawEntityOnScreen(this.width / 2, this.height / 2 + 28, 40, 0, 0, mc.player);
        if (hasData) {
            int morphineSecs = Math.round(damageModel.getMorphineTicks() / 20F);
            if (morphineSecs > 0)
                drawCenteredString(this.mc.fontRenderer, I18n.format("gui.morphine_left", morphineSecs), this.guiLeft + (xSize / 2), this.guiTop + ySize - 29, 0xFFFFFF);
            drawCenteredString(this.mc.fontRenderer, I18n.format("gui.apply_hint"), this.guiLeft + (xSize / 2), this.guiTop + ySize - (morphineSecs == 0 ? 21 : 11), 0xFFFFFF);
            this.mc.getTextureManager().bindTexture(Gui.ICONS);
            drawHealth(damageModel.HEAD, false, 20);
            drawHealth(damageModel.LEFT_ARM, false, 50);
            drawHealth(damageModel.LEFT_LEG, false, 80);
            drawHealth(damageModel.BODY, true, 20);
            drawHealth(damageModel.RIGHT_ARM, true, 50);
            drawHealth(damageModel.RIGHT_LEG, true, 80);
            //TODO color the critical parts of the player red?
        } else {
            drawCenteredString(this.mc.fontRenderer, "Waiting for data...", this.guiLeft + (xSize / 2), this.guiTop + ySize - 21, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
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
            throw new UnsupportedOperationException("Can only draw up to 8 hearts!");
        int toDraw = Math.min(4, Math.round(available / 2F));
        if (maxHealth > 8) {
            GlStateManager.translate(0, 5, 0);
            int toDrawSecond = (int) ((available - 8F) / 2F) + 1;
            if (toDrawSecond > 0)
                renderTexturedModalRects(toDrawSecond, true, halfTextureX, textureX, textureY);
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
        if (button.id < 7 && hasData) {
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
}
