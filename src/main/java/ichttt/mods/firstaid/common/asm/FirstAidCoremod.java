package ichttt.mods.firstaid.common.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("FirstAid CoreMod")
@IFMLLoadingPlugin.SortingIndex(1025)
@IFMLLoadingPlugin.TransformerExclusions("ichttt.mods.firstaid.common.")
public class FirstAidCoremod implements IFMLLoadingPlugin {
    public static final Logger LOGGER = LogManager.getLogger("SleepWellCore");

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {"ichttt.mods.firstaid.common.asm.PotionTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
