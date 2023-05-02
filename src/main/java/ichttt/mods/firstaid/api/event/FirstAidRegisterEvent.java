package ichttt.mods.firstaid.api.event;

import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * Base class for all first aid registry events
 * <br>
 * All events are fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS} once the server starts
 */
public class FirstAidRegisterEvent extends Event {
    private final Level level;

    public FirstAidRegisterEvent(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
