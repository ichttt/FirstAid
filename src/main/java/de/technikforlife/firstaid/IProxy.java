package de.technikforlife.firstaid;

public interface IProxy {
    void init();

    default void showGuiApplyHealth() {}

    default void healClient(float amount) {}
}
