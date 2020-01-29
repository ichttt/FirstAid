/*
 * FirstAid
 * Copyright (C) 2017-2020
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.client.gui;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.ClientProxy;
import ichttt.mods.firstaid.client.HUDHandler;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.network.MessageApplyHealingItem;
import ichttt.mods.firstaid.common.network.MessageClientRequest;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiHealthScreen extends GuiScreen {
    public static final int xSize = 256;
    public static final int ySize = 137;
    public static final ItemStack BED_ITEMSTACK = new ItemStack(Items.BED);
    private static final DecimalFormat FORMAT = new DecimalFormat("##.#");

    public static GuiHealthScreen INSTANCE;
    public static boolean isOpen = false;

    private final AbstractPlayerDamageModel damageModel;
    private final List<GuiHoldButton> holdButtons = new ArrayList<>();
    private final boolean disableButtons;
    private final float bedScaleFactor = EventCalendar.isGuiFun() ? 1.5F : 1.25F;

    public int guiLeft;
    public int guiTop;
    private GuiButton head, leftArm, leftLeg, leftFoot, body, rightArm, rightLeg, rightFoot;
    private EnumHand activeHand;

    public GuiHealthScreen(AbstractPlayerDamageModel damageModel) {
        this.damageModel = damageModel;
        disableButtons = true;
    }

    public GuiHealthScreen(AbstractPlayerDamageModel damageModel, EnumHand activeHand) {
        this.damageModel = damageModel;
        this.activeHand = activeHand;
        disableButtons = false;
    }

    @Override
    public void initGui() {
        isOpen = true;
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;

        head = new GuiHoldButton(1, this.guiLeft + 4, this.guiTop + 8, 52, 20, I18n.format("gui.head"), false);
        this.buttonList.add(head);

        leftArm = new GuiHoldButton(2, this.guiLeft + 4, this.guiTop + 33, 52, 20, I18n.format("gui.left_arm"), false);
        this.buttonList.add(leftArm);
        leftLeg = new GuiHoldButton(3, this.guiLeft + 4, this.guiTop + 58, 52, 20, I18n.format("gui.left_leg"), false);
        this.buttonList.add(leftLeg);
        leftFoot = new GuiHoldButton(4, this.guiLeft + 4, this.guiTop + 83, 52, 20, I18n.format("gui.left_foot"), false);
        this.buttonList.add(leftFoot);

        body = new GuiHoldButton(5, this.guiLeft + 199, this.guiTop + 8, 52, 20, I18n.format("gui.body"), true);
        this.buttonList.add(body);

        rightArm = new GuiHoldButton(6, this.guiLeft + 199, this.guiTop + 33, 52, 20, I18n.format("gui.right_arm"), true);
        this.buttonList.add(rightArm);
        rightLeg = new GuiHoldButton(7, this.guiLeft + 199, this.guiTop + 58, 52, 20, I18n.format("gui.right_leg"), true);
        this.buttonList.add(rightLeg);
        rightFoot = new GuiHoldButton(8, this.guiLeft + 199, this.guiTop + 83, 52, 20, I18n.format("gui.right_foot"), true);
        this.buttonList.add(rightFoot);

        if (disableButtons) {
            head.enabled = false;
            leftArm.enabled = false;
            leftLeg.enabled = false;
            leftFoot.enabled = false;
            body.enabled = false;
            rightArm.enabled = false;
            rightLeg.enabled = false;
            rightFoot.enabled = false;
        }

        GuiButton buttonCancel = new GuiButton(9, this.width / 2 - 100, this.height - 50, I18n.format("gui.cancel"));
        this.buttonList.add(buttonCancel);

        if (this.mc.gameSettings.showDebugInfo) {
            GuiButton refresh = new GuiButton(10, this.guiLeft + 218, this.guiTop + 115, 36, 20, "resync");
            this.buttonList.add(refresh);
        }

        holdButtons.clear();
        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiHoldButton) {
                Integer holdTime = activeHand == null ? null : FirstAidRegistryImpl.INSTANCE.getPartHealingTime(mc.player.getHeldItem(activeHand));
                if (holdTime == null)
                    holdTime = Integer.MAX_VALUE;
                ((GuiHoldButton) button).setup(holdTime, button.width / ((float) HUDHandler.INSTANCE.getMaxLength()));
                holdButtons.add((GuiHoldButton) button);
            }
        }

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        //Setup background
        this.drawDefaultBackground();
        this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + xSize, this.guiTop + ySize, -16777216, -16777216);
        this.mc.getTextureManager().bindTexture(HealthRenderUtils.GUI_LOCATION);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
        //Player
        int entityLookX = this.guiLeft + (xSize / 2) - mouseX;
        int entityLookY = this.guiTop + 20 - mouseY;
        if (EventCalendar.isGuiFun()) {
            entityLookX = -entityLookX;
            entityLookY = -entityLookY;
        }
        GuiInventory.drawEntityOnScreen(this.width / 2, this.height / 2 + 30, 45, entityLookX, entityLookY, mc.player);

        //Button
        super.drawScreen(mouseX, mouseY, partialTicks);

        //Text info
        int morphineTicks = damageModel.getMorphineTicks();
        if (morphineTicks > 0)
            drawCenteredString(this.mc.fontRenderer, I18n.format("gui.morphine_left", StringUtils.ticksToElapsedTime(morphineTicks)), this.guiLeft + (xSize / 2), this.guiTop + ySize - (this.activeHand == null ? 21 : 29), 0xFFFFFF);
        if (this.activeHand != null)
            drawCenteredString(this.mc.fontRenderer, I18n.format("gui.apply_hint"), this.guiLeft + (xSize / 2), this.guiTop + ySize - (morphineTicks == 0 ? 21 : 11), 0xFFFFFF);

        //Health
        this.mc.getTextureManager().bindTexture(Gui.ICONS);
        GlStateManager.color(1F, 1F, 1F, 1F);
        if (FirstAid.isSynced) {
            GlStateManager.pushMatrix();
            drawHealth(damageModel.HEAD, false, 14);
            drawHealth(damageModel.LEFT_ARM, false, 39);
            drawHealth(damageModel.LEFT_LEG, false, 64);
            drawHealth(damageModel.LEFT_FOOT, false, 89);
            drawHealth(damageModel.BODY, true, 14);
            drawHealth(damageModel.RIGHT_ARM, true, 39);
            drawHealth(damageModel.RIGHT_LEG, true, 64);
            drawHealth(damageModel.RIGHT_FOOT, true, 89);
            GlStateManager.popMatrix();
        }

        //Tooltip
        GlStateManager.pushMatrix();
        tooltipButton(head, damageModel.HEAD, mouseX, mouseY);
        tooltipButton(leftArm, damageModel.LEFT_ARM, mouseX, mouseY);
        tooltipButton(leftLeg, damageModel.LEFT_LEG, mouseX, mouseY);
        tooltipButton(leftFoot, damageModel.LEFT_FOOT, mouseX, mouseY);
        tooltipButton(body, damageModel.BODY, mouseX, mouseY);
        tooltipButton(rightArm, damageModel.RIGHT_ARM, mouseX, mouseY);
        tooltipButton(rightLeg, damageModel.RIGHT_LEG, mouseX, mouseY);
        tooltipButton(rightFoot, damageModel.RIGHT_FOOT, mouseX, mouseY);
        GlStateManager.popMatrix();

        //Sleep info setup
        double sleepHealing = FirstAidConfig.externalHealing.sleepHealPercentage;
        int renderBedX = Math.round(guiLeft / bedScaleFactor) + 2;
        int renderBedY = Math.round((guiTop + ySize) / bedScaleFactor) - 18;
        int bedX = (int) (renderBedX * bedScaleFactor);
        int bedY = (int) (renderBedY * bedScaleFactor);

        //Sleep info icon
        GlStateManager.pushMatrix();
        if (sleepHealing > 0D) RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.scale(bedScaleFactor, bedScaleFactor, bedScaleFactor);
        mc.getRenderItem().renderItemAndEffectIntoGUI(null, BED_ITEMSTACK, renderBedX, renderBedY);
        GlStateManager.popMatrix();

        //Sleep info tooltip
        if (mouseX >= bedX && mouseY >= bedY && mouseX < bedX + (16 * bedScaleFactor) && mouseY < bedY + (16 * bedScaleFactor)) {
            String s = sleepHealing == 0D ? I18n.format("gui.no_sleep_heal") : I18n.format("gui.sleep_heal_amount", FORMAT.format(sleepHealing * 100));
            drawHoveringText(s, mouseX, mouseY);
        }

        holdButtonMouseCallback(mouseX, mouseY, true); //callback: check if buttons are finish
        //TODO color the critical parts of the player red?
    }

    private void tooltipButton(GuiButton button, AbstractDamageablePart part, int mouseX, int mouseY) {
        boolean enabled = part.activeHealer == null;
        if (!enabled && button.isMouseOver()) {
            drawHoveringText(Arrays.asList(I18n.format("gui.active_item") + ": " + I18n.format(part.activeHealer.stack.getTranslationKey() + ".name"), I18n.format("gui.next_heal", Math.round((part.activeHealer.ticksPerHeal - part.activeHealer.getTicksPassed()) / 20F))), mouseX, mouseY);
        }
        if (!disableButtons)
            button.enabled = enabled;
    }

    public void drawHealth(AbstractDamageablePart damageablePart, boolean right, int yOffset) {
        GlStateManager.pushMatrix();
        int xTranslation = guiLeft + (right ? getRightOffset(damageablePart) : 57);
        HealthRenderUtils.drawHealth(damageablePart, xTranslation, guiTop + yOffset, this, true);
        GlStateManager.popMatrix();
    }

    private static int getRightOffset(AbstractDamageablePart damageablePart) {
        if (HealthRenderUtils.drawAsString(damageablePart, true))
            return 200 - 40;
        return 200 - Math.min(40, HealthRenderUtils.getMaxHearts(damageablePart.getMaxHealth()) * 9 + HealthRenderUtils.getMaxHearts(damageablePart.getAbsorption()) * 9 + 2);
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
            FirstAid.NETWORKING.sendToServer(new MessageApplyHealingItem(playerPart, activeHand));
            //TODO notify the user somehow (sound?)
            AbstractDamageablePart part = damageModel.getFromEnum(playerPart);
            part.activeHealer = FirstAidRegistryImpl.INSTANCE.getPartHealer(mc.player.getHeldItem(this.activeHand));
        } else if (button.id == 10) {
            FirstAid.NETWORKING.sendToServer(new MessageClientRequest(MessageClientRequest.Type.REQUEST_REFRESH));
            FirstAid.LOGGER.info("Requesting refresh");
            mc.player.sendStatusMessage(new TextComponentString("Re-downloading health data from server..."), true);
        }
        mc.displayGuiScreen(null);
    }

    protected void holdButtonMouseCallback(int mouseX, int mouseY, boolean renderPass) {
        for (GuiHoldButton button : this.holdButtons) {
            if (button.mousePressed(mc, mouseX, mouseY)) {
                //VANILLA COPY: GuiScreen#mouseClicked(without forge events)
                this.selectedButton = button;
                button.playPressSound(this.mc.getSoundHandler());
                this.actionPerformed(button);
            } else if (renderPass) {
                int timeLeft = button.getTimeLeft();
                if (timeLeft != -1) {
                    float timeInSecs = (timeLeft / 1000F);
                    if (timeInSecs < 0)
                        timeInSecs = 0;
                    this.mc.getTextureManager().bindTexture(HealthRenderUtils.GUI_LOCATION);
                    this.drawTexturedModalRect(button.x + (button.isRightSide ? 56 : - 25), button.y - 2, button.isRightSide ? 2 : 0, 169, 22, 24);
                    this.mc.fontRenderer.drawString(HealthRenderUtils.TEXT_FORMAT.format(timeInSecs), button.x  + (button.isRightSide ? 60 : -20), button.y + 6, 0xFFFFFF);
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        holdButtonMouseCallback(mouseX, mouseY, false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        INSTANCE = null;
        isOpen = false;
    }

    public List<GuiButton> getButtons() {
        return buttonList;
    }
}
