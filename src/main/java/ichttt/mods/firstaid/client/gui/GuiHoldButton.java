/*
 * FirstAid
 * Copyright (C) 2017-2018
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Util;

public class GuiHoldButton extends GuiButton {
    private int holdTime;
    private float textScaleFactor;
    public final boolean isRightSide;
    private long pressStart = -1;
    private boolean pressed = false;

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
    public void render(int mouseX, int mouseY, float partialTicks) {
        //TODO progress bar?
        drawScaledTextButton(mouseX, mouseY);
    }

    //VANILLA COPY: GuiButton#render
    private void drawScaledTextButton(int mouseX, int mouseY) {
        if (this.visible)
        {
            Minecraft minecraft = Minecraft.getInstance();
            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            this.renderBg(minecraft, mouseX, mouseY);
            int j = 14737632;
            if (packedFGColor != 0)
            {
                j = packedFGColor;
            }
            else
            if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }

            //CHANGE: scale text if not fitting
            if (textScaleFactor != 1F) {
                GlStateManager.pushMatrix();
                GlStateManager.scalef(textScaleFactor, textScaleFactor, 1);
                this.drawCenteredString(fontrenderer, this.displayString, Math.round((this.x + this.width / 2F) / textScaleFactor), Math.round((this.y + (this.height - 8) / 2F) / textScaleFactor), j);
                GlStateManager.popMatrix();
            } else
                this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        boolean result = super.isPressable(mouseX, mouseY);
        if (result) {
            pressStart = Util.milliTime();
            pressed = true;
        }
        return result || super.mouseClicked(mouseX, mouseY, button); //TODO check if this is called every tick. We need a method that fires every tick so we can check if sufficient time passed
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        boolean result = pressStart != -1 && (super.mouseReleased(mouseX, mouseY, button));
        if (result) {
            pressStart = -1;
            pressed = false;
        }
        return result;
    }

    /**
     * The time left in in ms
     */
    public int getTimeLeft() {
        if (pressStart == -1)
            return -1;
        return (int) Math.max(0L, holdTime - (Util.milliTime() - pressStart));
    }
}
