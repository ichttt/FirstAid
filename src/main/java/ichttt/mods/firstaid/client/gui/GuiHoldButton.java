/*
 * FirstAid
 * Copyright (C) 2017-2019
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

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class GuiHoldButton extends AbstractButton {
    public final int id;
    private int holdTime;
    private float textScaleFactor;
    public final boolean isRightSide;
    private long pressStart = -1;

    public GuiHoldButton(int id, int x, int y, int widthIn, int heightIn, String buttonText, boolean isRightSide) {
        super(x, y, widthIn, heightIn, buttonText);
        this.id = id;
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
            minecraft.getTextureManager().bindTexture(WIDGETS_LOCATION);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
            int i = this.getYImage(this.isHovered());
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.blit(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
            this.blit(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            this.renderBg(minecraft, mouseX, mouseY);
            int j = getFGColor();

            //CHANGE: scale text if not fitting
            if (textScaleFactor != 1F) {
                GlStateManager.pushMatrix();
                GlStateManager.scalef(textScaleFactor, textScaleFactor, 1);
                this.drawCenteredString(fontrenderer, this.getMessage(), Math.round((this.x + this.width / 2F) / textScaleFactor), Math.round((this.y + (this.height - 8) / 2F) / textScaleFactor), j);
                GlStateManager.popMatrix();
            } else
                this.drawCenteredString(fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }

//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        if (button != 0) return false;
//        boolean result = super.isPressable(mouseX, mouseY);
//        if (result) {
//            pressStart = Util.milliTime();
//        }
//        return result || super.mouseClicked(mouseX, mouseY, button);
//    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        boolean result = pressStart != -1 && (super.mouseReleased(mouseX, mouseY, button));
        if (result) {
            pressStart = -1;
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

    public void reset() {
        pressStart = -1;
    }

    @Override
    public void onPress() {
        pressStart = Util.milliTime();
    }
}
