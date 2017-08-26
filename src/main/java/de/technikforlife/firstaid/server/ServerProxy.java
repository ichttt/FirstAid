package de.technikforlife.firstaid.server;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.IProxy;

public class ServerProxy implements IProxy {

    @Override
    public void init() {
        FirstAid.logger.debug("Loading ServerProxy");
    }
}
