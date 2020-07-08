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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.ClientHooks;
import ichttt.mods.firstaid.client.HUDHandler;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.network.MessageApplyHealingItem;
import ichttt.mods.firstaid.common.network.MessageClientRequest;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiHealthScreen extends Screen {
    public static final int xSize = 256;
    public static final int ySize = 137;
    public static final ItemStack BED_ITEMSTACK = new ItemStack(Items.RED_BED);
    private static final DecimalFormat FORMAT = new DecimalFormat("##.#");

    public static GuiHealthScreen INSTANCE;
    public static boolean isOpen = false;

    private final AbstractPlayerDamageModel damageModel;
    private final List<GuiHoldButton> holdButtons = new ArrayList<>();
    private final boolean disableButtons;
    private final float bedScaleFactor = EventCalendar.isGuiFun() ? 1.5F : 1.25F;

    public int guiLeft;
    public int guiTop;
    public AbstractButton cancelButton;
    private AbstractButton head, leftArm, leftLeg, leftFoot, body, rightArm, rightLeg, rightFoot;
    private Hand activeHand;

    public GuiHealthScreen(AbstractPlayerDamageModel damageModel) {
        super(new TranslationTextComponent("firstaid.gui.healthscreen"));
        this.damageModel = damageModel;
        disableButtons = true;
    }

    public GuiHealthScreen(AbstractPlayerDamageModel damageModel, Hand activeHand) {
        super(new TranslationTextComponent("firstaid.gui.healthscreen"));
        this.damageModel = damageModel;
        this.activeHand = activeHand;
        disableButtons = false;
    }

    @Override
    public void init() {
        isOpen = true;
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;

        head = new GuiHoldButton(1, this.guiLeft + 4, this.guiTop + 8, 52, 20, new TranslationTextComponent("firstaid.gui.head"), false);
        addButton(head);

        leftArm = new GuiHoldButton(2, this.guiLeft + 4, this.guiTop + 33, 52, 20, new TranslationTextComponent("firstaid.gui.left_arm"), false);
        addButton(leftArm);
        leftLeg = new GuiHoldButton(3, this.guiLeft + 4, this.guiTop + 58, 52, 20, new TranslationTextComponent("firstaid.gui.left_leg"), false);
        addButton(leftLeg);
        leftFoot = new GuiHoldButton(4, this.guiLeft + 4, this.guiTop + 83, 52, 20, new TranslationTextComponent("firstaid.gui.left_foot"), false);
        addButton(leftFoot);

        body = new GuiHoldButton(5, this.guiLeft + 199, this.guiTop + 8, 52, 20, new TranslationTextComponent("firstaid.gui.body"), true);
        addButton(body);

        rightArm = new GuiHoldButton(6, this.guiLeft + 199, this.guiTop + 33, 52, 20, new TranslationTextComponent("firstaid.gui.right_arm"), true);
        addButton(rightArm);
        rightLeg = new GuiHoldButton(7, this.guiLeft + 199, this.guiTop + 58, 52, 20, new TranslationTextComponent("firstaid.gui.right_leg"), true);
        addButton(rightLeg);
        rightFoot = new GuiHoldButton(8, this.guiLeft + 199, this.guiTop + 83, 52, 20, new TranslationTextComponent("firstaid.gui.right_foot"), true);
        addButton(rightFoot);

        if (disableButtons) {
            head.active = false;
            leftArm.active = false;
            leftLeg.active = false;
            leftFoot.active = false;
            body.active = false;
            rightArm.active = false;
            rightLeg.active = false;
            rightFoot.active = false;
        }

        cancelButton = new Button(this.width / 2 - 100, this.height - 50, 200, 20, new TranslationTextComponent("gui.cancel"), button -> onClose());
        addButton(cancelButton);

        if (this.minecraft.gameSettings.showDebugInfo) {
            Button refresh = new Button(this.guiLeft + 218, this.guiTop + 115, 36, 20, new StringTextComponent("resync"), button -> {
                FirstAid.NETWORKING.sendToServer(new MessageClientRequest(MessageClientRequest.Type.REQUEST_REFRESH));
                FirstAid.LOGGER.info("Requesting refresh");
                minecraft.player.sendStatusMessage(new StringTextComponent("Re-downloading health data from server..."), true);
                onClose();
            });
            addButton(refresh);
        }

        holdButtons.clear();
        for (Widget button : this.buttons) {
            if (button instanceof GuiHoldButton) {
                Integer holdTime = activeHand == null ? null : FirstAidRegistryImpl.INSTANCE.getPartHealingTime(minecraft.player.getHeldItem(activeHand));
                if (holdTime == null) holdTime = Integer.MAX_VALUE;
                ((GuiHoldButton) button).setup(holdTime, button.getWidth() / ((float) HUDHandler.INSTANCE.getMaxLength()));
                holdButtons.add((GuiHoldButton) button);
            }
        }

        super.init();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        //Setup background
        this.renderBackground(stack);
        this.fillGradient(stack, this.guiLeft, this.guiTop, this.guiLeft + xSize, this.guiTop + ySize, -16777216, -16777216);
        this.minecraft.getTextureManager().bindTexture(HealthRenderUtils.GUI_LOCATION);
        this.blit(stack, this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
        //Player
        int entityLookX = this.guiLeft + (xSize / 2) - mouseX;
        int entityLookY = this.guiTop + 20 - mouseY;
        if (EventCalendar.isGuiFun()) {
            entityLookX = -entityLookX;
            entityLookY = -entityLookY;
        }
        InventoryScreen.drawEntityOnScreen(this.width / 2, this.height / 2 + 30, 45, entityLookX, entityLookY, minecraft.player);

        //Button
        super.render(stack, mouseX, mouseY, partialTicks);

        //Text info
        int morphineTicks = damageModel.getMorphineTicks();
        if (morphineTicks > 0)
            drawCenteredString(stack, this.minecraft.fontRenderer, I18n.format("firstaid.gui.morphine_left", StringUtils.ticksToElapsedTime(morphineTicks)), this.guiLeft + (xSize / 2), this.guiTop + ySize - (this.activeHand == null ? 21 : 29), 0xFFFFFF);
        if (this.activeHand != null)
            drawCenteredString(stack, this.minecraft.fontRenderer, I18n.format("firstaid.gui.apply_hint"), this.guiLeft + (xSize / 2), this.guiTop + ySize - (morphineTicks == 0 ? 21 : 11), 0xFFFFFF);

        //Health
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        drawHealth(stack, damageModel.HEAD, false, 14);
        drawHealth(stack, damageModel.LEFT_ARM, false, 39);
        drawHealth(stack, damageModel.LEFT_LEG, false, 64);
        drawHealth(stack, damageModel.LEFT_FOOT, false, 89);
        drawHealth(stack, damageModel.BODY, true, 14);
        drawHealth(stack, damageModel.RIGHT_ARM, true, 39);
        drawHealth(stack, damageModel.RIGHT_LEG, true, 64);
        drawHealth(stack, damageModel.RIGHT_FOOT, true, 89);

        //Tooltip
        stack.push();
        tooltipButton(stack, head, damageModel.HEAD, mouseX, mouseY);
        tooltipButton(stack, leftArm, damageModel.LEFT_ARM, mouseX, mouseY);
        tooltipButton(stack, leftLeg, damageModel.LEFT_LEG, mouseX, mouseY);
        tooltipButton(stack, leftFoot, damageModel.LEFT_FOOT, mouseX, mouseY);
        tooltipButton(stack, body, damageModel.BODY, mouseX, mouseY);
        tooltipButton(stack, rightArm, damageModel.RIGHT_ARM, mouseX, mouseY);
        tooltipButton(stack, rightLeg, damageModel.RIGHT_LEG, mouseX, mouseY);
        tooltipButton(stack, rightFoot, damageModel.RIGHT_FOOT, mouseX, mouseY);
        stack.pop();

        //Sleep info setup
        double sleepHealing = FirstAidConfig.SERVER.sleepHealPercentage.get();
        int renderBedX = Math.round(guiLeft / bedScaleFactor) + 2;
        int renderBedY = Math.round((guiTop + ySize) / bedScaleFactor) - 18;
        int bedX = (int) (renderBedX * bedScaleFactor);
        int bedY = (int) (renderBedY * bedScaleFactor);

        //Sleep info icon
        RenderSystem.pushMatrix();
        RenderSystem.scalef(bedScaleFactor, bedScaleFactor, bedScaleFactor);
        minecraft.getItemRenderer().renderItemAndEffectIntoGUI(null, BED_ITEMSTACK, renderBedX, renderBedY);
        RenderSystem.popMatrix();

        //Sleep info tooltip
        if (mouseX >= bedX && mouseY >= bedY && mouseX < bedX + (16 * bedScaleFactor) && mouseY < bedY + (16 * bedScaleFactor)) {
            ITextComponent s = sleepHealing == 0D ? new TranslationTextComponent("firstaid.gui.no_sleep_heal") : new TranslationTextComponent("firstaid.gui.sleep_heal_amount", FORMAT.format(sleepHealing * 100));
            renderTooltip(stack, s, mouseX, mouseY);
            RenderSystem.disableLighting();
        }

        holdButtonMouseCallback(stack); //callback: check if buttons are finish
    }

    private void tooltipButton(MatrixStack stack, AbstractButton button, AbstractDamageablePart part, int mouseX, int mouseY) {
        boolean enabled = part.activeHealer == null;
        if (!enabled && button.isHovered()) {
            renderTooltip(stack, Arrays.asList(new StringTextComponent(I18n.format("firstaid.gui.active_item") + ": " + I18n.format(part.activeHealer.stack.getTranslationKey())), new TranslationTextComponent("firstaid.gui.next_heal", Math.round((part.activeHealer.ticksPerHeal.getAsInt() - part.activeHealer.getTicksPassed()) / 20F))), mouseX, mouseY);
        }
        if (!disableButtons) button.active = enabled;
    }

    public void drawHealth(MatrixStack stack, AbstractDamageablePart damageablePart, boolean right, int yOffset) {
        stack.push();
        int xTranslation = guiLeft + (right ? getRightOffset(damageablePart) : 57);
        HealthRenderUtils.drawHealth(stack, damageablePart, xTranslation, guiTop + yOffset, this, true);
        stack.pop();
    }

    private static int getRightOffset(AbstractDamageablePart damageablePart) {
        if (HealthRenderUtils.drawAsString(damageablePart, true)) return 200 - 40;
        return 200 - Math.min(40, HealthRenderUtils.getMaxHearts(damageablePart.getMaxHealth()) * 9 + HealthRenderUtils.getMaxHearts(damageablePart.getAbsorption()) * 9 + 2);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
            return true;
        if (ClientHooks.showWounds.isActiveAndMatches(InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_))) {
            onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        holdButtonMouseCallback(null);
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public void mouseMoved(double xPos, double yPos) {
        for (GuiHoldButton holdButton : this.holdButtons) {
            holdButton.mouseMoved(xPos, yPos);
        }
    }

    protected void holdButtonMouseCallback(MatrixStack stack) {
        for (GuiHoldButton button : this.holdButtons) {
            int timeLeft = button.getTimeLeft();
            if (timeLeft == 0) {
                //We are officially done
                button.reset();
                EnumPlayerPart playerPart = EnumPlayerPart.VALUES[button.id - 1];
                FirstAid.NETWORKING.sendToServer(new MessageApplyHealingItem(playerPart, activeHand));
                AbstractDamageablePart part = damageModel.getFromEnum(playerPart);
                part.activeHealer = FirstAidRegistryImpl.INSTANCE.getPartHealer(minecraft.player.getHeldItem(this.activeHand));
                onClose();
            } else if (stack == null) {
                button.reset();
            } else if (timeLeft != -1) {
                float timeInSecs = (timeLeft / 1000F);
                if (timeInSecs < 0F) timeInSecs = 0F;
                this.minecraft.getTextureManager().bindTexture(HealthRenderUtils.GUI_LOCATION);
                this.blit(stack, button.x + (button.isRightSide ? 56 : -25), button.y - 2, button.isRightSide ? 2 : 0, 169, 22, 24);
                this.minecraft.fontRenderer.drawString(stack, HealthRenderUtils.TEXT_FORMAT.format(timeInSecs), button.x + (button.isRightSide ? 60 : -20), button.y + 6, 0xFFFFFF);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        INSTANCE = null;
        isOpen = false;
        super.onClose();
    }

    public List<Widget> getButtons() {
        return this.buttons;
    }
}
