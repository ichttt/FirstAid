package ichttt.mods.firstaid.common.apiimpl;

public class APIManager {

    public static void setupAPI() {
        DamageDistributionBuilderImpl.init();
        DebuffBuilderFactoryImpl.init();
        HealingItemApiHelperImpl.init();
    }

    public static void validateAPI() {
        DamageDistributionBuilderImpl.verify();
        DebuffBuilderFactoryImpl.verify();
        HealingItemApiHelperImpl.verify();
    }
}
