package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.EventHandler;
import de.technikforlife.firstaid.FirstAidConfig;
import de.technikforlife.firstaid.damagesystem.enums.EnumPlayerPart;
import de.technikforlife.firstaid.damagesystem.enums.EnumWoundState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class PlayerDamageModel implements INBTSerializable<NBTTagCompound>, Iterable<DamageablePart> {
    public final DamageablePart HEAD, LEFT_ARM, LEFT_LEG, LEFT_FOOT, BODY, RIGHT_ARM, RIGHT_LEG, RIGHT_FOOT;
    private int tickCounter;
    private int morphineTicksLeft = 0;

    public PlayerDamageModel() {
        this.HEAD = new DamageablePart(FirstAidConfig.damageSystem.maxHealthHead, true, EntityEquipmentSlot.HEAD, EnumPlayerPart.HEAD);
        this.LEFT_ARM = new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftArm, false, EntityEquipmentSlot.CHEST, EnumPlayerPart.LEFT_ARM);
        this.LEFT_LEG = new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftLeg, false, EntityEquipmentSlot.LEGS, EnumPlayerPart.LEFT_LEG);
        this.LEFT_FOOT = new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftFoot, false, EntityEquipmentSlot.FEET, EnumPlayerPart.LEFT_FOOT);
        this.BODY = new DamageablePart(FirstAidConfig.damageSystem.maxHealthBody, true, EntityEquipmentSlot.CHEST, EnumPlayerPart.BODY);
        this.RIGHT_ARM = new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightArm, false, EntityEquipmentSlot.CHEST, EnumPlayerPart.RIGHT_ARM);
        this.RIGHT_LEG = new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightLeg, false, EntityEquipmentSlot.LEGS, EnumPlayerPart.RIGHT_LEG);
        this.RIGHT_FOOT = new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightFoot, false, EntityEquipmentSlot.FEET, EnumPlayerPart.RIGHT_FOOT);
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
            case LEFT_FOOT:
                return LEFT_FOOT;
            case RIGHT_FOOT:
                return RIGHT_FOOT;
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
        tagCompound.setTag("leftFoot", LEFT_FOOT.serializeNBT());
        tagCompound.setTag("body", BODY.serializeNBT());
        tagCompound.setTag("rightArm", RIGHT_ARM.serializeNBT());
        tagCompound.setTag("rightLeg", RIGHT_LEG.serializeNBT());
        tagCompound.setTag("rightFoot", RIGHT_FOOT.serializeNBT());
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
            LEFT_FOOT.deserializeNBT((NBTTagCompound) nbt.getTag("leftFoot"));
            BODY.deserializeNBT((NBTTagCompound) nbt.getTag("body"));
            RIGHT_ARM.deserializeNBT((NBTTagCompound) nbt.getTag("rightArm"));
            RIGHT_LEG.deserializeNBT((NBTTagCompound) nbt.getTag("rightLeg"));
            RIGHT_FOOT.deserializeNBT((NBTTagCompound) nbt.getTag("rightFoot"));
        }
        morphineTicksLeft = nbt.getInteger("morphineTicks");
    }

    private static void deserializeNBT_legacy(NBTTagCompound nbt, String key, DamageablePart part) {
        part.currentHealth = Math.min(nbt.getFloat(key + "Health"), part.maxHealth);
    }

    public void tick(World world, EntityPlayer player, boolean fake) {
        if (player.isDead || player.getHealth() <= 0F)
            return;
        if (FirstAidConfig.allowNaturalRegeneration) {
            boolean isFullLife = true;
            for (DamageablePart part : this) {
                if (part.currentHealth != part.maxHealth) {
                    isFullLife = false;
                    break;
                }
            }
            player.setHealth(isFullLife ? player.getMaxHealth() : 2F);
        }

        if (morphineTicksLeft <= 0) {
            tickCounter++;
            if (tickCounter >= 140) {
                tickCounter = 0;
                if (!fake) {
                    for (PlayerDamageDebuff debuff : PlayerDamageDebuff.possibleDebuffs)
                        debuff.applyDebuff(player, this);
                }
            }
        } else {
            morphineTicksLeft--;
        }
        forEach(part -> part.tick(world, player, fake));
    }

    public void applyMorphine() {
        morphineTicksLeft = (EventHandler.rand.nextInt(2) * 20 * 45) + 20 * 210;
    }

    public int getMorphineTicks() {
        return morphineTicksLeft;
    }

    public DamageablePart getHealthyFoot() {
        DamageablePart playerPart = EventHandler.rand.nextBoolean() ? LEFT_FOOT : RIGHT_FOOT;
        if (playerPart.getWoundState() == EnumWoundState.WOUNDED_HEAVY) {
            if (playerPart == LEFT_FOOT)
                playerPart = RIGHT_FOOT;
            else
                playerPart = LEFT_FOOT;
        }
        return playerPart;
    }

    @Override
    @Nonnull
    public Iterator<DamageablePart> iterator() {
        return new Iterator<DamageablePart>() {
            byte count = 1;
            @Override
            public boolean hasNext() {
                return count <= 8;
            }

            @Override
            public DamageablePart next() {
                DamageablePart part = getFromEnum(EnumPlayerPart.fromID(count));
                count++;
                return part;
            }
        };
    }
}
