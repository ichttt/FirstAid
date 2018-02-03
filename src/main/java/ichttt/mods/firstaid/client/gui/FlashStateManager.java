package ichttt.mods.firstaid.client.gui;

public class FlashStateManager {
    private long startTime;
    private int currentState = 0;

    public void setActive(long startTime) {
        this.startTime = startTime;
        currentState = 1;
    }

    public boolean update(long worldTime) {
        if (currentState == 0)
            return false;
        if (worldTime - startTime > 150) {
            startTime = worldTime;
            currentState++;
            if (currentState >= 8)
                currentState = 0;
        }
        return currentState % 2 == 0;
    }
}
