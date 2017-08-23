package de.technikforlife.firstaid.damagesystem;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Random;
import java.util.UUID;

public class PlayerDamageModel implements INBTSerializable<NBTTagCompound> {
    public final DamageablePart HEAD, LEFT_ARM, LEFT_LEG, BODY, RIGHT_ARM, RIGHT_LEG;

    public PlayerDamageModel(UUID playerUUID) {
//        this.playerUUID = player.getPersistentID();
        this.HEAD = new DamageablePart(4.0F, true, playerUUID);
        this.LEFT_ARM = new DamageablePart(4.0F, false, playerUUID);
        this.LEFT_LEG = new DamageablePart(4.0F, false, playerUUID);
        this.BODY = new DamageablePart(6.0F, true, playerUUID);
        this.RIGHT_ARM = new DamageablePart(4.0F, false, playerUUID);
        this.RIGHT_LEG = new DamageablePart(4.0F, false, playerUUID);
    }

    public DamageablePart getFromEnum(EnumPlayerPart part) {
        switch (part) {
            case HEAD:
                return HEAD;
            case LEFT_ARM:
                return LEFT_ARM;
            case LEFT_LEG:
                return LEFT_LEG;
            case BODY:
                return BODY;
            case RIGHT_ARM:
                return RIGHT_ARM;
            case RIGHT_LEG:
                return RIGHT_LEG;
            default:
                throw new RuntimeException("Unknown enum " + part);
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

    public void tick(World world) {
        HEAD.tick(world);
        LEFT_ARM.tick(world);
        LEFT_LEG.tick(world);
        BODY.tick(world);
        RIGHT_ARM.tick(world);
        RIGHT_LEG.tick(world);
    }
}
