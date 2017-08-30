package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.EventHandler;
import de.technikforlife.firstaid.FirstAidConfig;
import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerDamageModel implements INBTSerializable<NBTTagCompound> {
    public final DamageablePart HEAD, LEFT_ARM, LEFT_LEG, BODY, RIGHT_ARM, RIGHT_LEG;
    private int tickCounter;
    private int morphineTicksLeft = 0;

    public PlayerDamageModel() {
        this.HEAD = new DamageablePart(FirstAidConfig.damageSystem.maxHealthHead, true);
        this.LEFT_ARM = new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftArm, false);
        this.LEFT_LEG = new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftLeg, false);
        this.BODY = new DamageablePart(FirstAidConfig.damageSystem.maxHealthBody, true);
        this.RIGHT_ARM = new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightArm, false);
        this.RIGHT_LEG = new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightLeg, false);
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
        tagCompound.setInteger("morphineTicks", morphineTicksLeft);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        deserializeNBT(nbt, "head", HEAD);
        deserializeNBT(nbt, "leftArm", LEFT_ARM);
        deserializeNBT(nbt, "leftLeg", LEFT_LEG);
        deserializeNBT(nbt, "body", BODY);
        deserializeNBT(nbt, "rightArm", RIGHT_ARM);
        deserializeNBT(nbt, "rightLeg", RIGHT_LEG);
        morphineTicksLeft = nbt.getInteger("morphineTicks");
    }

    private static void deserializeNBT(NBTTagCompound nbt, String key, DamageablePart part) {
        part.currentHealth = Math.min(nbt.getFloat(key + "Health"), part.maxHealth);
    }

    public void tick(World world, EntityPlayer player) {
        if (player.isCreative())
            return;
        if (morphineTicksLeft <= 0) {
            tickCounter++;
            if (tickCounter >= 400) {
                tickCounter = 0;
                for (PlayerDamageDebuff debuff : PlayerDamageDebuff.possibleDebuffs)
                    debuff.applyDebuff(player, this);
            }
        } else {
            morphineTicksLeft--;
        }
        HEAD.tick(world, player);
        LEFT_ARM.tick(world, player);
        LEFT_LEG.tick(world, player);
        BODY.tick(world, player);
        RIGHT_ARM.tick(world, player);
        RIGHT_LEG.tick(world, player);
    }

    public void applyMorphine() {
        morphineTicksLeft = (EventHandler.rand.nextInt(2) * 20 * 45) + 20 * 210;
    }
}
