package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerDamageDebuff {
    public static final ArrayList<PlayerDamageDebuff> possibleDebuffs = new ArrayList<>();
    static {
        possibleDebuffs.add(new PlayerDamageDebuff("slowness", 0.1F, false, EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG));
        possibleDebuffs.add(new PlayerDamageDebuff("mining_fatigue", 0.25F, false, EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM));
        possibleDebuffs.add(new PlayerDamageDebuff("blindness", 0.9F, true, EnumPlayerPart.HEAD));
        possibleDebuffs.add(new PlayerDamageDebuff("nausea", 0.92F, true, EnumPlayerPart.HEAD));
        possibleDebuffs.add(new PlayerDamageDebuff("weakness", 0.5F, false, EnumPlayerPart.BODY));
        possibleDebuffs.trimToSize();
    }

    private final Potion effect;
    private final float chance;
    private final boolean canBeRemovedEntirely;
    private final EnumPlayerPart[] activeParts;

    public PlayerDamageDebuff(String potionName, float chance, boolean canBeRemovedEntirely, EnumPlayerPart... activeParts) {
        this.effect = Objects.requireNonNull(ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionName)));
        this.chance = chance;
        this.canBeRemovedEntirely = canBeRemovedEntirely;
        this.activeParts = activeParts;
    }

    public void applyDebuff(EntityPlayer player, PlayerDamageModel playerDamageModel) {
        int count = 0;
        for (EnumPlayerPart part : activeParts) {
            count += getDebuffCount(playerDamageModel.getFromEnum(part));
        }
        if (count == 0)
            return;
        float rdm = (float) Math.random();
        while (rdm < chance && count > 0) {
            rdm = rdm  * 2;
            count--;
        }
        if (count <= 0) {
            if (canBeRemovedEntirely)
                return;
            else
                count = 0;
        }
        count = Math.min(count, 3);
        player.addPotionEffect(new PotionEffect(effect, 200, count, false, false));
    }

    private static int getDebuffCount(DamageablePart part) {
        int count = 0;
        switch (part.getWoundState()) {
            case WOUNDED_HEAVY:
                count++;
            case WOUNDED_LIGHT:
                count++;
                break;
        }
        return count;
    }
}
