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

package ichttt.mods.firstaid.client.tutorial;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.ClientProxy;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.network.MessageClientRequest;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTutorial extends GuiScreen {
    private final GuiHealthScreen parent;
    private final AbstractPlayerDamageModel demoModel;
    private int guiTop;
    private final TutorialAction action;

    @SuppressWarnings("deprecation") // we still need this method
    public GuiTutorial() {
        this.demoModel = PlayerDamageModel.create();
        this.parent = new GuiHealthScreen(demoModel);
        this.action = new TutorialAction(this);

        this.action.addTextWrapper("firstaid.tutorial.welcome");
        this.action.addTextWrapper("firstaid.tutorial.line1");
        this.action.addTextWrapper("firstaid.tutorial.line2");
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.LEFT_FOOT.damage(4F, null, false));
        this.action.addTextWrapper("firstaid.tutorial.line3");
        //We need the deprecated version
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.applyMorphine());
        this.action.addTextWrapper("firstaid.tutorial.line4");
        this.action.addTextWrapper("firstaid.tutorial.line5");
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.LEFT_FOOT.heal(3F, null, false));
        if (FirstAidConfig.externalHealing.sleepHealPercentage != 0D)
            this.action.addTextWrapper("firstaid.tutorial.sleephint");
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
        this.buttonList.add(new GuiButton(0, parent.guiLeft + GuiHealthScreen.xSize - 34, guiTop + 4, 32, 20, ">"));
        this.buttonList.addAll(parent.getButtons());
        parent.getButtons().clear();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 9) {
            FirstAid.NETWORKING.sendToServer(new MessageClientRequest(MessageClientRequest.Type.TUTORIAL_COMPLETE));
            mc.displayGuiScreen(null);
        } else if (button.id == 0) {
            if (action.hasNext())
                this.action.next();
            else {
                FirstAid.NETWORKING.sendToServer(new MessageClientRequest(MessageClientRequest.Type.TUTORIAL_COMPLETE));
                mc.displayGuiScreen(new GuiHealthScreen(mc.player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null)));
            }
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
        mc.getTextureManager().bindTexture(HealthRenderUtils.GUI_LOCATION);
        drawTexturedModalRect(parent.guiLeft, guiTop ,0, 139, GuiHealthScreen.xSize, 28);
        GlStateManager.pushMatrix();
        this.action.draw();
        GlStateManager.popMatrix();
        drawCenteredString(mc.fontRenderer, I18n.format("firstaid.tutorial.notice"), parent.guiLeft + (GuiHealthScreen.xSize / 2), parent.guiTop + 140, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        GuiHealthScreen.isOpen = false;
    }
}
