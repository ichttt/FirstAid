package ichttt.mods.firstaid.server;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.IProxy;
import ichttt.mods.firstaid.damagesystem.PlayerDamageModel;

@SuppressWarnings("unused")
public class ServerProxy implements IProxy {

    @Override
    public void init() {
        FirstAid.logger.debug("Loading ServerProxy");
    }
}
