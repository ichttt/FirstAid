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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;

public class GuiHoldButton extends GuiButton {
    private int holdTime;
    private float textScaleFactor;
    public final boolean isRightSide;
    private long pressStart = -1;

    public GuiHoldButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, boolean isRightSide) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.isRightSide = isRightSide;
    }

    public void setup(int holdTime, float textScaleFactor) {
        this.holdTime = holdTime;
        if (textScaleFactor > 0.95F)
            textScaleFactor = 1F;
        if (textScaleFactor < 0.8F)
            textScaleFactor = 0.8F;
        this.textScaleFactor = textScaleFactor;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!Mouse.isCreated()) {
            FirstAid.LOGGER.warn("Mouse is not created!");
            return;
        }
        //TODO progress bar?

        boolean pressed = Mouse.isButtonDown(0);
        if (pressed)
            pressed = super.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY);

        if (!pressed)
            pressStart = -1;
        else if (pressStart == -1)
            pressStart = Minecraft.getSystemTime();

        drawScaledTextButton(mc, mouseX, mouseY, partialTicks);
    }

    //VANILLA COPY: GuiButton#drawButton
    private void drawScaledTextButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;

            if (packedFGColour != 0)
            {
                j = packedFGColour;
            }
            else
            if (!this.enabled)
            {
                j = 10526880;
            }
            else if (this.hovered)
            {
                j = 16777120;
            }

            //CHANGE: scale text if not fitting
            if (textScaleFactor != 1F) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(textScaleFactor, textScaleFactor, 1);
                this.drawCenteredString(fontrenderer, this.displayString, Math.round((this.x + this.width / 2) / textScaleFactor), Math.round((this.y + (this.height - 8) / 2) / textScaleFactor), j);
                GlStateManager.popMatrix();
            } else
                this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean result = pressStart != -1 && (pressStart + holdTime) <= Minecraft.getSystemTime() && super.mousePressed(mc, mouseX, mouseY);
        if (result)
            pressStart = -1;
        return result;
    }

    /**
     * The time left in in ms
     */
    public int getTimeLeft() {
        if (pressStart == -1)
            return -1;
        return (int) Math.max(0L, holdTime - (Minecraft.getSystemTime() - pressStart));
    }
}
