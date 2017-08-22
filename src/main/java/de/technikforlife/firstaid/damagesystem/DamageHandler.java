package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.damagesystem.capability.DataManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;
import java.util.Random;

public class DamageHandler {
    private static final Random rand = new Random();

    @SubscribeEvent(priority = EventPriority.LOW) //so all other can modify their damage first, and we apply after that
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.hasCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null))
            return;
        PlayerDamageModel damageModel = Objects.requireNonNull(entity.getCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null));
        DamageSource source = event.getSource();
        String sourceType = source.damageType;
        DamageablePart toDamage;
        switch (sourceType) {
            case "fall":
            case "hotFloor":
                toDamage = rand.nextBoolean() ? damageModel.LEFT_LEG : damageModel.RIGHT_LEG;
                break;
            case "fallingBlock":
            case "anvil":
                toDamage = damageModel.HEAD;
                break;
            case "starve":
                toDamage = damageModel.BODY;
                break;
            default:
                toDamage = damageModel.getRandomPart();
                break;
        }
        toDamage.damage(event.getAmount());
//        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void registerCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity obj = event.getObject();
        if (obj instanceof EntityPlayer && !(obj instanceof FakePlayer))
            event.addCapability(DataManager.IDENTIFIER, new DataManager((EntityPlayer) obj));
    }
}
