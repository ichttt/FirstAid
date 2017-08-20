package de.technikforlife.firstaid.client;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.IProxy;

public class ClientProxy implements IProxy {

    @Override
    public void init() {
        FirstAid.logger.info("Loading Client");
    }
}
