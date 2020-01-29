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

package ichttt.mods.firstaid.client.config;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.client.ClientProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@SideOnly(Side.CLIENT)
public class GuiConfigScreen extends GuiConfig {
    private final List<GuiConfigEntries.IConfigEntry> removedEntries = new ArrayList<>();
    private static boolean isAdvanced = false;
    private GuiButton button;

    public GuiConfigScreen(GuiScreen parentScreen) {
        super(parentScreen, FirstAid.MODID, FirstAid.NAME);
    }

    public GuiConfigScreen(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2) {
        super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
    }

    @Override
    public void initGui() {
        this.entryList.listEntries.addAll(removedEntries);
        super.initGui();
        this.entryList.listEntries.removeAll(removedEntries);
        setupButton();
    }

    private void setupButton() {
        if (this.button != null)
            this.buttonList.remove(this.button);
        this.button = new GuiButton(1999, 0, 0, 80, 20, "Show " + (isAdvanced ? "less" : "more"));
        this.buttonList.add(button);
        if (isAdvanced) {
            this.entryList.listEntries.addAll(removedEntries);
            removedEntries.clear();
        } else {
            ListIterator<GuiConfigEntries.IConfigEntry> iterator = this.entryList.listEntries.listIterator();
            while (iterator.hasNext()) {
                GuiConfigEntries.IConfigEntry next = iterator.next();
                if (ClientProxy.advancedConfigOptions.stream().anyMatch(entry -> entry.property.matches(next.getConfigElement()))) {
                    removedEntries.add(next);
                    iterator.remove();
                }
            }
        }
        for (int i = 0; i < this.entryList.listEntries.size(); i++) {
            GuiConfigEntries.IConfigEntry guiEntry = this.entryList.listEntries.get(i);
            if (guiEntry instanceof GuiConfigEntries.CategoryEntry && !(guiEntry instanceof CustomCategoryEntry)) {
                GuiConfigEntries.CategoryEntry entry = (GuiConfigEntries.CategoryEntry) guiEntry;
                this.entryList.listEntries.set(i, new CustomCategoryEntry(this, this.entryList, entry.getConfigElement()));
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1999) {
            isAdvanced = !isAdvanced;
            setupButton();
        } else if (button.id == 2000 && this.parentScreen instanceof GuiModList) { //save and exit
            isAdvanced = false;
            super.actionPerformed(button);
        } else
            super.actionPerformed(button);
    }
}
