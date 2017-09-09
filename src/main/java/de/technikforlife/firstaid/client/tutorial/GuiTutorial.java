package de.technikforlife.firstaid.client.tutorial;

import de.technikforlife.firstaid.client.ClientProxy;
import de.technikforlife.firstaid.client.GuiApplyHealthItem;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiTutorial extends GuiScreen {
    private final GuiApplyHealthItem parent;
    private final PlayerDamageModel demoModel;
    private int guiTop;
    private final TutorialAction action;

    public GuiTutorial() {
        this.parent = new GuiApplyHealthItem();
        this.demoModel = new PlayerDamageModel();
        this.parent.onReceiveData(demoModel);
        this.action = new TutorialAction(this);

        this.action.addTextWrapper(new TextWrapper("Welcome to FirstAid! This tutorial will guide you through this mod and explain the different mechanics"));
        this.action.addTextWrapper(new TextWrapper("As you may already have noticed, the vanilla health bar is gone. It has been replaced by this system."));
        this.action.addTextWrapper(new TextWrapper("Every part of your body has it's own health. So if you fall from a high place..."));
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.LEFT_LEG.damage(4F));
        this.action.addTextWrapper(new TextWrapper("One of your legs will be damaged! This may cause some debuffs to apply, like slowness for damaged feet, mining fatigue for damaged arms and so one"));
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.applyMorphine());
        this.action.addTextWrapper(new TextWrapper("This effects may be bypassed by taking morphine or healing the wound."));
        this.action.addTextWrapper(new TextWrapper("Wounds can be healed by applying bandages or plaster (right click the item)"));
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.LEFT_LEG.heal(3F, null));
        this.action.addTextWrapper(new TextWrapper("A player can die, if a critical organ's health (head or body) drop to zero."));
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.HEAD.damage(16F));
        this.action.addTextWrapper(new TextWrapper("In this case, the player would be dead, no matter how much health the other organs have."));
        this.action.addTextWrapper(new TextWrapper("If you want to see your current health, just press " + ClientProxy.showWounds.getDisplayName() + " to open this interface with your health"));
        this.action.addActionCallable(guiTutorial -> {
            guiTutorial.buttonList.remove(0);
            this.buttonList.add(new GuiButton(1, parent.guiLeft + GuiApplyHealthItem.xSize - 34, guiTop + 4, 32, 20, "end"));
        });
        this.action.addTextWrapper(new TextWrapper("That is the end of the tutorial."));

        this.action.next();
    }

    @Override
    public void initGui() {
        parent.setWorldAndResolution(mc, this.width, this.height);
        guiTop = parent.guiTop - 30;
        this.buttonList.add(new GuiButton(0, parent.guiLeft + GuiApplyHealthItem.xSize - 34, guiTop + 4, 32, 20, ">"));
        this.buttonList.addAll(parent.getButtons());
        parent.getButtons().clear();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 7 || button.id == 1)
            Minecraft.getMinecraft().displayGuiScreen(null);
        else if (button.id == 0) {
            this.action.next();
        }
    }

    public void drawOffsetString(String s, int yOffset) {
        drawString(mc.fontRenderer, s, parent.guiLeft + 30, guiTop + yOffset, 0xFFFFFF);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        parent.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();
        mc.getTextureManager().bindTexture(GuiApplyHealthItem.GUI_LOCATION);
        drawTexturedModalRect(parent.guiLeft, guiTop ,0, 135, GuiApplyHealthItem.xSize, 28);
        GlStateManager.pushMatrix();
        this.action.draw();
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
