package de.technikforlife.firstaid.client;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.EnumPlayerPart;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class GuiShowWounds extends GuiScreen {
    private static final ResourceLocation GUI_BACKGROUND = new ResourceLocation(FirstAid.MODID, "textures/gui/show_wounds.png");
    private static final int xSize = 248;
    private static final int ySize = 132;

    private int guiLeft;
    private int guiTop;

    private final PlayerDamageModel damageModel;

    public GuiShowWounds(PlayerDamageModel damageModel) {
        this.damageModel = damageModel;
    }

    @Override
    public void initGui() { //TODO I18N
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;
        super.initGui();
        GuiButton buttonCancel = new GuiButton(1, this.width / 2 - 100, this.height - 50, I18n.format("gui.cancel"));
        this.buttonList.add(buttonCancel);

        GuiButton applyHead = new GuiButton(2, this.guiLeft + 5, this.guiTop + 14, 64, 20, "Head");
        this.buttonList.add(applyHead);

        GuiButton applyLeftArm = new GuiButton(3, this.guiLeft + 5, this.guiTop + 44, 64, 20, "Left Arm");
        this.buttonList.add(applyLeftArm);
        GuiButton applyLeftLeg = new GuiButton(4, this.guiLeft + 5, this.guiTop + 74, 64, 20, "Left Leg");
        this.buttonList.add(applyLeftLeg);

        GuiButton applyBody = new GuiButton(5, this.guiLeft + 175, this.guiTop + 14, 64, 20, "Body");
        this.buttonList.add(applyBody);

        GuiButton applyRightArm = new GuiButton(6, this.guiLeft + 175, this.guiTop + 44, 64, 20, "Right Arm");
        this.buttonList.add(applyRightArm);
        GuiButton applyRightLeg = new GuiButton(7, this.guiLeft + 175, this.guiTop + 74, 64, 20, "Right Leg");
        this.buttonList.add(applyRightLeg);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
//        GlStateManager.colorMask(true, false, false, true);
        this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + xSize, this.guiTop + ySize, -16777216, -16777216);
        this.mc.getTextureManager().bindTexture(GUI_BACKGROUND);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
        GuiInventory.drawEntityOnScreen(this.width / 2, this.height / 2 + 28, 40, 0, 0, mc.player);
        drawCenteredString(this.mc.fontRenderer, "Pick where to apply the bandage", this.guiLeft + (xSize / 2), this.guiTop + ySize - 21, 0xFFFFFF);
        //TODO draw hearts based on the damage model
        //TODO color the critical parts of the player red?
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id != 1) {
            EnumPlayerPart playerPart = EnumPlayerPart.fromID((byte) (button.id - 1));
            //TODO msg bandage applied
        }
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
