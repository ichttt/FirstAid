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
        tagCompound.setTag("head", HEAD.serializeNBT());
        tagCompound.setTag("leftArm", LEFT_ARM.serializeNBT());
        tagCompound.setTag("leftLeg", LEFT_LEG.serializeNBT());
        tagCompound.setTag("body", BODY.serializeNBT());
        tagCompound.setTag("rightArm", RIGHT_ARM.serializeNBT());
        tagCompound.setTag("rightLeg", RIGHT_LEG.serializeNBT());
        tagCompound.setInteger("morphineTicks", morphineTicksLeft);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("headHealth")) {
            deserializeNBT_legacy(nbt, "head", HEAD);
            deserializeNBT_legacy(nbt, "leftArm", LEFT_ARM);
            deserializeNBT_legacy(nbt, "leftLeg", LEFT_LEG);
            deserializeNBT_legacy(nbt, "body", BODY);
            deserializeNBT_legacy(nbt, "rightArm", RIGHT_ARM);
            deserializeNBT_legacy(nbt, "rightLeg", RIGHT_LEG);
        } else {
            HEAD.deserializeNBT((NBTTagCompound) nbt.getTag("head"));
            LEFT_ARM.deserializeNBT((NBTTagCompound) nbt.getTag("leftArm"));
            LEFT_LEG.deserializeNBT((NBTTagCompound) nbt.getTag("leftLeg"));
            BODY.deserializeNBT((NBTTagCompound) nbt.getTag("body"));
            RIGHT_ARM.deserializeNBT((NBTTagCompound) nbt.getTag("rightArm"));
            RIGHT_LEG.deserializeNBT((NBTTagCompound) nbt.getTag("rightLeg"));
        }
        morphineTicksLeft = nbt.getInteger("morphineTicks");
    }

    private static void deserializeNBT_legacy(NBTTagCompound nbt, String key, DamageablePart part) {
        part.currentHealth = Math.min(nbt.getFloat(key + "Health"), part.maxHealth);
    }

    public void tick(World world, EntityPlayer player, boolean fake) {
        if (morphineTicksLeft <= 0) {
            tickCounter++;
            if (tickCounter >= 160) {
                tickCounter = 0;
                if (!fake) {
                    for (PlayerDamageDebuff debuff : PlayerDamageDebuff.possibleDebuffs)
                        debuff.applyDebuff(player, this);
                }
            }
        } else {
            morphineTicksLeft--;
        }
        HEAD.tick(world, player, fake);
        LEFT_ARM.tick(world, player, fake);
        LEFT_LEG.tick(world, player, fake);
        BODY.tick(world, player, fake);
        RIGHT_ARM.tick(world, player, fake);
        RIGHT_LEG.tick(world, player, fake);
    }

    public void applyMorphine() {
        morphineTicksLeft = (EventHandler.rand.nextInt(2) * 20 * 45) + 20 * 210;
    }

    public int getMorphineTicks() {
        return morphineTicksLeft;
    }
}
