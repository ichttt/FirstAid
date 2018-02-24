package ichttt.mods.firstaid.client.config;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.config.ExtraConfigManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class GuiConfigScreen extends GuiConfig {
    private List<GuiConfigEntries.IConfigEntry> allConfigEntries;
    private boolean isAdvanced = false;
    private GuiButton button;

    private static List<IConfigElement> collectConfigElements(Class<?>[] configClasses) {
        List<IConfigElement> toReturn;
        if(configClasses.length == 1) {
            toReturn = ConfigElement.from(configClasses[0]).getChildElements();
        }
        else {
            toReturn = new ArrayList<>();
            for(Class<?> clazz : configClasses) {
                toReturn.add(ConfigElement.from(clazz));
            }
        }
        toReturn.sort(Comparator.comparing(e -> I18n.format(e.getLanguageKey())));
        return toReturn;
    }

    public GuiConfigScreen(GuiScreen parentScreen) {
        super(parentScreen, collectConfigElements(ConfigManager.getModConfigClasses(FirstAid.MODID)), FirstAid.MODID, null, false, false, FirstAid.NAME, null);
    }

    @Override
    public void initGui() {
        super.initGui();
        allConfigEntries = this.entryList.listEntries;
        setupButton();
    }

    private void setupButton() {
        this.button = new GuiButton(1999, 0, 0, 80, 20, "Show " + (isAdvanced ? "less" : "more"));
        this.buttonList.add(button);
        this.entryList.listEntries = new ArrayList<>(this.allConfigEntries);
        if (!isAdvanced) {
            Iterator<GuiConfigEntries.IConfigEntry> iterator = this.entryList.listEntries.iterator();
            while (iterator.hasNext()) {
                GuiConfigEntries.IConfigEntry guiEntry = iterator.next();
                if (ExtraConfigManager.advancedConfigOptions.stream().anyMatch(entry -> entry.getRight().matches(guiEntry.getConfigElement()))) {
                    System.out.println("Removing " + guiEntry.getName() + "(" + guiEntry + ")");
                    iterator.remove();
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1999) {
            this.isAdvanced = !this.isAdvanced;
            this.buttonList.remove(this.button);
            setupButton();
        } else
            super.actionPerformed(button);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        isAdvanced = false;
    }
}
