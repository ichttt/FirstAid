package ichttt.mods.firstaid.api;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class CapabilityExtendedHealthSystem {

    @CapabilityInject(AbstractPlayerDamageModel.class)
    public static Capability<AbstractPlayerDamageModel> INSTANCE;
}
