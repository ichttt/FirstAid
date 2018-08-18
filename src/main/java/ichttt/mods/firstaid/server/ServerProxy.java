package ichttt.mods.firstaid.server;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.IProxy;

@SuppressWarnings("unused")
public class ServerProxy implements IProxy {

    @Override
    public void preInit() {
        FirstAid.LOGGER.debug("Loading ServerProxy");
    }
}
