package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.damagesystem.capability.DataManager;
import de.technikforlife.firstaid.network.MessageDamagePlayerPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Objects;
import java.util.Random;

public class DamageHandler {
    public static final Random rand = new Random();

    @SubscribeEvent(priority = EventPriority.LOW) //so all other can modify their damage first, and we apply after that
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.getEntityWorld().isRemote || !entity.hasCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null))
            return;
        PlayerDamageModel damageModel = Objects.requireNonNull(entity.getCapability(CapabilityExtendedHealthSystem.CAP_EXTENDED_HEALTH_SYSTEM, null));
        DamageSource source = event.getSource();
        String sourceType = source.damageType;
        EnumPlayerPart toDamage;
        float amountToDamage = event.getAmount();
        switch (sourceType) {
            case "fall":
            case "hotFloor":
                toDamage = rand.nextBoolean() ? EnumPlayerPart.LEFT_LEG : EnumPlayerPart.RIGHT_LEG;
                break;
            case "fallingBlock":
            case "anvil":
                toDamage = EnumPlayerPart.HEAD;
                break;
            case "starve":
                toDamage = EnumPlayerPart.BODY;
                break;
            default:
                toDamage = EnumPlayerPart.getRandomPart();
                break;
        }
        FirstAid.NETWORKING.sendTo(new MessageDamagePlayerPart(toDamage, amountToDamage), (EntityPlayerMP) entity);
        DamageablePart partToDamage = damageModel.getFromEnum(toDamage);
        if (partToDamage.damage(amountToDamage) && partToDamage.canCauseDeath) {
            event.setAmount(Float.MAX_VALUE);
        }
    }

    @SubscribeEvent
    public static void registerCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity obj = event.getObject();
        if (obj instanceof EntityPlayer && !(obj instanceof FakePlayer))
            event.addCapability(DataManager.IDENTIFIER, new DataManager((EntityPlayer) obj));
    }

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            DataManager.tickAll(event.world);
    }
}
