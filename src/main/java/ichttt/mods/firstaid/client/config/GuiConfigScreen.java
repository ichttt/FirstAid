package ichttt.mods.firstaid.client.config;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.config.ExtraConfigManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

public class GuiConfigScreen extends GuiConfig {
    private List<GuiConfigEntries.IConfigEntry> allConfigEntries;
    private static boolean isAdvanced = false;
    private GuiButton button;

    public GuiConfigScreen(GuiScreen parentScreen) {
        super(parentScreen, FirstAid.MODID, FirstAid.NAME);
        allConfigEntries = this.entryList.listEntries;
    }

    public GuiConfigScreen(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2) {
        super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
        allConfigEntries = this.entryList.listEntries;
    }

    @Override
    public void initGui() {
        if (this.entryList == null || this.needsRefresh)
        {
            this.entryList = new GuiConfigEntries(this, mc);
            this.needsRefresh = false;
            this.allConfigEntries = this.entryList.listEntries;
        }
        super.initGui();
//        allConfigEntries = this.entryList.listEntries;
        setupButton();
//        if (isAdvanced) {
//            this.buttonList.clear();
//            super.initGui();
//            allConfigEntries = this.entryList.listEntries;
//            setupButton();
//        }
    }

    private void setupButton() {
        if (this.button != null)
            this.buttonList.remove(this.button);
        this.button = new GuiButton(1999, 0, 0, 80, 20, "Show " + (isAdvanced ? "less" : "more"));
        this.buttonList.add(button);
        this.entryList.listEntries = new ArrayList<>(this.allConfigEntries);
        if (!isAdvanced)
            this.entryList.listEntries.removeIf(guiEntry -> ExtraConfigManager.advancedConfigOptions.stream().anyMatch(entry -> entry.getRight().matches(guiEntry.getConfigElement())));
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
        } else
            super.actionPerformed(button);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
//        isAdvanced = false;
    }
}
