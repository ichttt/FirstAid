package ichttt.mods.firstaid.common.compat.playerrevive;

import com.google.common.base.Preconditions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public class PRCompatManager {
    private static IPRCompatHandler handler = new NoopPRCompatHandler();

    public static IPRCompatHandler getHandler() {
        return handler;
    }

    public static void init() {
        if (ModList.get().isLoaded("playerrevive")) {
            loadCompatClass();
        }
    }

    private static void loadCompatClass() {
        if (PRPresentCompatHandler.canUse()) {
            handler = new PRPresentCompatHandler();
            MinecraftForge.EVENT_BUS.register(PlayerReviveEventHandler.class);
        }
    }
}
