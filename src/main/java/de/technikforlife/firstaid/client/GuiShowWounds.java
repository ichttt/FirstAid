package de.technikforlife.firstaid.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.io.IOException;

public class GuiShowWounds extends GuiScreen {
    private final EntityPlayer player;
    private ScaledResolution scaledRes;

    public GuiShowWounds(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public void initGui() {
        scaledRes = new ScaledResolution(Minecraft.getMinecraft());
        GuiButton buttonOK = new GuiButton(1, this.width / 2 - 100, this.height - 50, "Done");
        this.buttonList.add(buttonOK);
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//        GlStateManager.color(1F, 0F, 0F, 1F);
        Color startColor = new Color(0x414141);
        Color endColor = new Color(0x5F5F5F);
        int height = scaledRes.getScaledHeight() / 4;
        drawGradientRect(this.width / 2 - 52, height - 40, this.width / 2 + 48, this.height - height - 30, startColor.getRGB(), endColor.getRGB());
        GuiInventory.drawEntityOnScreen(this.width / 2, this.height / 2 + 20, scaledRes.getScaledHeight() / 4,0, 0, player);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1:
                Minecraft.getMinecraft().displayGuiScreen(null);
                break;
            default:
                super.actionPerformed(button);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
