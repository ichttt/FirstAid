package de.technikforlife.firstaid.damagesystem;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerDamageModel implements INBTSerializable<NBTTagCompound> {
    public final DamagablePart HEAD, LEFT_ARM, LEFT_LEG, BODY, RIGHT_ARM, RIGHT_LEG;

    public PlayerDamageModel() {
//        this.playerUUID = player.getPersistentID();
        this.HEAD = new DamagablePart(2.0F, true);
        this.LEFT_ARM = new DamagablePart(2.0F, false);
        this.LEFT_LEG = new DamagablePart(2.0F, false);
        this.BODY = new DamagablePart(3.0F, true);
        this.RIGHT_ARM = new DamagablePart(2.0F, false);
        this.RIGHT_LEG = new DamagablePart(2.0F, false);
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
