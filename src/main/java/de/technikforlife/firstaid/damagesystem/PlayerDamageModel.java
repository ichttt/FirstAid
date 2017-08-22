package de.technikforlife.firstaid.damagesystem;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Random;

public class PlayerDamageModel implements INBTSerializable<NBTTagCompound> {
    private static final Random rand = new Random();
    public final DamageablePart HEAD, LEFT_ARM, LEFT_LEG, BODY, RIGHT_ARM, RIGHT_LEG;

    public PlayerDamageModel() {
//        this.playerUUID = player.getPersistentID();
        this.HEAD = new DamageablePart(4.0F, true);
        this.LEFT_ARM = new DamageablePart(4.0F, false);
        this.LEFT_LEG = new DamageablePart(4.0F, false);
        this.BODY = new DamageablePart(6.0F, true);
        this.RIGHT_ARM = new DamageablePart(4.0F, false);
        this.RIGHT_LEG = new DamageablePart(4.0F, false);
    }

    public DamageablePart getRandomPart() {
        int value = rand.nextInt(6);
        switch (value) {
            case 0:
                return HEAD;
            case 1:
                return LEFT_ARM;
            case 2:
                return LEFT_LEG;
            case 3:
                return BODY;
            case 4:
                return RIGHT_ARM;
            case 5:
                return RIGHT_LEG;
            default:
                throw new RuntimeException("Invalid number " + value);
        }
    }


    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setFloat("headHealth", HEAD.currentHealth);
        tagCompound.setFloat("leftArmHealth", LEFT_ARM.currentHealth);
        tagCompound.setFloat("leftLegHealth", LEFT_LEG.currentHealth);
        tagCompound.setFloat("bodyHealth", BODY.currentHealth);
        tagCompound.setFloat("rightArmHealth", RIGHT_ARM.currentHealth);
        tagCompound.setFloat("rightLegHealth", RIGHT_LEG.currentHealth);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        HEAD.currentHealth = nbt.getFloat("headHealth");
        LEFT_ARM.currentHealth = nbt.getFloat("leftArmHealth");
        LEFT_LEG.currentHealth = nbt.getFloat("leftLegHealth");
        BODY.currentHealth = nbt.getFloat("bodyHealth");
        RIGHT_ARM.currentHealth = nbt.getFloat("rightArmHealth");
        RIGHT_LEG.currentHealth = nbt.getFloat("rightLegHealth");
    }
}
