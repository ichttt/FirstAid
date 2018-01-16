package ichttt.mods.firstaid.server;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.IProxy;

@SuppressWarnings("unused")
public class ServerProxy implements IProxy {

    @Override
    public void init() {
        FirstAid.logger.debug("Loading ServerProxy");
        //On dedicated server, it starts ticking before any player joins.
        FirstAid.activeDamageConfig = FirstAidConfig.damageSystem;
        FirstAid.activeHealingConfig = FirstAidConfig.externalHealing;
        FirstAid.scaleMaxHealth = FirstAidConfig.scaleMaxHealth;
    }
}
