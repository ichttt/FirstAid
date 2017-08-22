package de.technikforlife.firstaid.damagesystem;

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
        float amount = event.getAmount();
        switch (sourceType) {
            case "fall":
            case "hotFloor":
                if (rand.nextBoolean()) damageModel.LEFT_LEG.damage(amount);
                else damageModel.RIGHT_LEG.damage(amount);
                break;
            case "fallingBlock":
            case "anvil":
                damageModel.HEAD.damage(amount);
                break;
            case "starve":
                damageModel.BODY.damage(amount);
                break;
            default:
                int value = rand.nextInt(6);
                switch (value) {
                    case 0:
                        damageModel.HEAD.damage(value);
                        break;
                    case 1:
                        damageModel.LEFT_ARM.damage(value);
                        break;
                    case 2:
                        damageModel.LEFT_ARM.damage(value);
                        break;
                    case 3:
                        damageModel.BODY.damage(value);
                        break;
                    case 4:
                        damageModel.RIGHT_ARM.damage(value);
                        break;
                    case 5:
                        damageModel.RIGHT_LEG.damage(value);
                        break;
                    default:
                        throw new RuntimeException("Invalid number " + value);
                }
                break;
        }
//        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void registerCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity obj = event.getObject();
        if (obj instanceof EntityPlayer && !(obj instanceof FakePlayer))
            event.addCapability(DataManager.IDENTIFIER, new DataManager((EntityPlayer) obj));
    }
}
