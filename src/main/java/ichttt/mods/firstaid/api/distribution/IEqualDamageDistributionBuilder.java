package ichttt.mods.firstaid.api.distribution;

public interface IEqualDamageDistributionBuilder extends IDamageDistributionBuilder {

    /**
     * If this is called, the distribution will try to avoid killing people. If all non-critical health is depleted
     * and there is undistributed damage, the distribution will still kill the player to redistribute the missing damage.
     * @return The current builder
     */
    IEqualDamageDistributionBuilder tryNoKill();
}
