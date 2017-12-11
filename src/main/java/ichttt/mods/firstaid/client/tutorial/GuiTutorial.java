package ichttt.mods.firstaid.client.tutorial;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.ClientProxy;
import ichttt.mods.firstaid.client.gui.GuiApplyHealthItem;
import ichttt.mods.firstaid.client.gui.GuiUtils;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.network.MessageHasTutorial;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiTutorial extends GuiScreen {
    private final GuiApplyHealthItem parent;
    private final AbstractPlayerDamageModel demoModel;
    private int guiTop;
    private final TutorialAction action;

    public GuiTutorial() {
        this.demoModel = PlayerDamageModel.create();
        this.parent = new GuiApplyHealthItem(demoModel);
        this.action = new TutorialAction(this);

        this.action.addTextWrapper("firstaid.tutorial.welcome");
        this.action.addTextWrapper("firstaid.tutorial.line1");
        this.action.addTextWrapper("firstaid.tutorial.line2");
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.LEFT_FOOT.damage(4F, null, false));
        this.action.addTextWrapper("firstaid.tutorial.line3");
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.applyMorphine());
        this.action.addTextWrapper("firstaid.tutorial.line4");
        this.action.addTextWrapper("firstaid.tutorial.line5");
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.LEFT_FOOT.heal(3F, null, false));
        this.action.addTextWrapper("firstaid.tutorial.line6");
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.HEAD.damage(16F, null, false));
        this.action.addTextWrapper("firstaid.tutorial.line7");
        this.action.addTextWrapper("firstaid.tutorial.line8", ClientProxy.showWounds.getDisplayName());
        this.action.addTextWrapper("firstaid.tutorial.end");

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
    protected void actionPerformed(GuiButton button) {
        if (button.id == 9) {
            FirstAid.NETWORKING.sendToServer(new MessageHasTutorial());
            mc.displayGuiScreen(null);
        } else if (button.id == 0) {
            if (action.hasNext())
                this.action.next();
            else {
                FirstAid.NETWORKING.sendToServer(new MessageHasTutorial());
                mc.displayGuiScreen(new GuiApplyHealthItem(PlayerDataManager.getDamageModel(mc.player)));
            }
        }
    }

    public void drawOffsetString(String s, int yOffset) {
        drawString(mc.fontRendererObj, s, parent.guiLeft + 30, guiTop + yOffset, 0xFFFFFF);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        parent.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();
        mc.getTextureManager().bindTexture(GuiUtils.GUI_LOCATION);
        drawTexturedModalRect(parent.guiLeft, guiTop ,0, 139, GuiApplyHealthItem.xSize, 28);
        GlStateManager.pushMatrix();
        this.action.draw();
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
