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

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
    public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontrenderer = minecraft.fontRenderer;
        minecraft.getTextureManager().bindTexture(WIDGETS_LOCATION);
        if (this.active)
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        else
            RenderSystem.color4f(0.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.blit(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(minecraft, p_renderButton_1_, p_renderButton_2_);
        int j = 0xFFFFFF;

        //CHANGE: scale text if not fitting
        if (textScaleFactor != 1F) {
            RenderSystem.pushMatrix();
            RenderSystem.scalef(textScaleFactor, textScaleFactor, 1);
            this.drawCenteredString(fontrenderer, this.getMessage(), Math.round((this.x + this.width / 2F) / textScaleFactor), Math.round((this.y + (this.height - 8) / 2F) / textScaleFactor), j);
            RenderSystem.popMatrix();
        } else
            this.drawCenteredString(fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    public void mouseMoved(double xPos, double yPos) {
        super.mouseMoved(xPos, yPos);
        if (pressStart != -1 && !isMouseOver(xPos, yPos))
            pressStart = -1;
    }

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
