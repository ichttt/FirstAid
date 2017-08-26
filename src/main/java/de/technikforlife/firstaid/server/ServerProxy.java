package de.technikforlife.firstaid.server;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.IProxy;

@SuppressWarnings("unused")
public class ServerProxy implements IProxy {

    @Override
    public void init() {
        FirstAid.logger.debug("Loading ServerProxy");
    }
}
