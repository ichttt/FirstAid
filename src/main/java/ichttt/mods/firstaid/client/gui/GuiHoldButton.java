/*
 * FirstAid
 * Copyright (C) 2017-2024
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

import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class GuiHoldButton extends AbstractButton {
    public final int id;
    private int holdTime;
    private float textScaleFactor;
    public final boolean isRightSide;
    private long pressStart = -1;

    public GuiHoldButton(int id, int x, int y, int widthIn, int heightIn, Component buttonText, boolean isRightSide) {
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
    public void mouseMoved(double xPos, double yPos) {
        super.mouseMoved(xPos, yPos);
        if (pressStart != -1 && !isMouseOver(xPos, yPos))
            pressStart = -1;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (pressStart != -1 && !focused)
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
     * The time left in ms
     */
    public int getTimeLeft() {
        if (pressStart == -1)
            return -1;
        return (int) Math.max(0L, holdTime - (Util.getMillis() - pressStart));
    }

    public void reset() {
        pressStart = -1;
    }

    @Override
    public void onPress() {
        pressStart = Util.getMillis();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
