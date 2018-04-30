package ichttt.mods.firstaid.common.asm;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.Collections;

public class CoreModContainer extends DummyModContainer {

    public CoreModContainer() {
        super(new ModMetadata());
        ModMetadata metadata = getMetadata();
        metadata.authorList = Collections.singletonList("ichttt");
        metadata.description = "The CoreMod component for FirstAid";
        metadata.modId = "firstaidcore";
        metadata.name = "First Aid Core";
        metadata.parent = "firstaid";
        metadata.version = "1.0.0";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
