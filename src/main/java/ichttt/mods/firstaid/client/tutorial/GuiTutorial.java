/*
 * FirstAid
 * Copyright (C) 2017-2021
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

import com.mojang.blaze3d.matrix.MatrixStack;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.ClientHooks;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.network.MessageClientRequest;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiTutorial extends Screen {
    private final GuiHealthScreen parent;
    private final AbstractPlayerDamageModel demoModel;
    private int guiTop;
    private final TutorialAction action;

    @SuppressWarnings("deprecation") // we still need this method
    public GuiTutorial(PlayerEntity player) {
        super(new TranslationTextComponent("firstaid.tutorial"));
        this.demoModel = PlayerDamageModel.create();
        this.parent = new GuiHealthScreen(demoModel, player);
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
        if (FirstAidConfig.SERVER.sleepHealPercentage.get() != 0D)
            this.action.addTextWrapper("firstaid.tutorial.sleephint");
        this.action.addTextWrapper("firstaid.tutorial.line6");
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.HEAD.damage(16F, null, false));
        this.action.addTextWrapper("firstaid.tutorial.line7");
        this.action.addTextWrapper("firstaid.tutorial.line8", ClientHooks.SHOW_WOUNDS.getTranslatedKeyMessage().getString());
        this.action.addTextWrapper("firstaid.tutorial.end");

        this.action.next();
    }

    @Override
    public void init() {
        parent.init(minecraft, this.width, this.height);
        guiTop = parent.guiTop - 30;
        addButton(new Button(parent.guiLeft + GuiHealthScreen.xSize - 34, guiTop + 4, 32, 20, new StringTextComponent(">"), button -> {
            if (action.hasNext()) GuiTutorial.this.action.next();
            else {
                FirstAid.NETWORKING.sendToServer(new MessageClientRequest(MessageClientRequest.Type.TUTORIAL_COMPLETE));
                minecraft.setScreen(new GuiHealthScreen(CommonUtils.getDamageModel(minecraft.player), minecraft.player));
            }
        }));
        for (Widget button : parent.getButtons()) {
            if (button == parent.cancelButton) {
                addButton(new Button(button.x, button.y, button.getWidth(), button.getHeight(), button.getMessage(), ignored -> {
                    FirstAid.NETWORKING.sendToServer(new MessageClientRequest(MessageClientRequest.Type.TUTORIAL_COMPLETE));
                    minecraft.setScreen(null);
                }));
                continue;
            }
            addButton(button);
        }
        parent.getButtons().clear();
    }

    public void drawOffsetString(MatrixStack stack, String s, int yOffset) {
        drawString(stack, minecraft.font, s, parent.guiLeft + 30, guiTop + yOffset, 0xFFFFFF);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        stack.pushPose();
        parent.render(stack, mouseX, mouseY, partialTicks);
        stack.popPose();
        minecraft.getTextureManager().bind(HealthRenderUtils.GUI_LOCATION);
        blit(stack, parent.guiLeft, guiTop, 0, 139, GuiHealthScreen.xSize, 28);
        stack.pushPose();
        this.action.draw(stack);
        stack.popPose();
        drawCenteredString(stack, minecraft.font, I18n.get("firstaid.tutorial.notice"), parent.guiLeft + (GuiHealthScreen.xSize / 2), parent.guiTop + 128, 0xFFFFFF);
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        GuiHealthScreen.isOpen = false;
    }
}
