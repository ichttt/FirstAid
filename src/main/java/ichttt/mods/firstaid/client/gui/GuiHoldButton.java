package ichttt.mods.firstaid.client.gui;

import ichttt.mods.firstaid.FirstAid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;

public class GuiHoldButton extends GuiButton {
    private static final int HOLD_TIME = 3000;
    public final boolean isRightSide;
    private long pressStart = -1;

    public GuiHoldButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, boolean isRightSide) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.isRightSide = isRightSide;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!Mouse.isCreated()) {
            FirstAid.logger.warn("Mouse is not created!");
            return;
        }

        boolean pressed = Mouse.isButtonDown(0);
        if (pressed)
            pressed = super.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY);

        if (!pressed)
            pressStart = -1;
        else if (pressStart == -1)
            pressStart = Minecraft.getSystemTime();

        super.drawButton(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean result = pressStart != -1 && (pressStart + HOLD_TIME) <= Minecraft.getSystemTime() && super.mousePressed(mc, mouseX, mouseY);
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
        return (int) Math.max(0L, HOLD_TIME - (Minecraft.getSystemTime() - pressStart));
    }
}
