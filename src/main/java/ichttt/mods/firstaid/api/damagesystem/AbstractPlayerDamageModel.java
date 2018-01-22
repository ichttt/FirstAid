package ichttt.mods.firstaid.api.damagesystem;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class AbstractPlayerDamageModel implements Iterable<AbstractDamageablePart>, INBTSerializable<NBTTagCompound> {
    public final AbstractDamageablePart HEAD;
    public final AbstractDamageablePart LEFT_ARM;
    public final AbstractDamageablePart LEFT_LEG;
    public final AbstractDamageablePart LEFT_FOOT;
    public final AbstractDamageablePart BODY;
    public final AbstractDamageablePart RIGHT_ARM;
    public final AbstractDamageablePart RIGHT_LEG;
    public final AbstractDamageablePart RIGHT_FOOT;
    public final boolean isTemp;
    public boolean hasTutorial;

    @Deprecated
    public AbstractPlayerDamageModel(AbstractDamageablePart head, AbstractDamageablePart leftArm, AbstractDamageablePart leftLeg, AbstractDamageablePart leftFoot, AbstractDamageablePart body, AbstractDamageablePart rightArm, AbstractDamageablePart rightLeg, AbstractDamageablePart rightFoot) {
        this(head, leftArm, leftLeg, leftFoot, body, rightArm, rightLeg, rightFoot, false);
    }

    public AbstractPlayerDamageModel(AbstractDamageablePart head, AbstractDamageablePart leftArm, AbstractDamageablePart leftLeg, AbstractDamageablePart leftFoot, AbstractDamageablePart body, AbstractDamageablePart rightArm, AbstractDamageablePart rightLeg, AbstractDamageablePart rightFoot, boolean isTemp) {
        this.HEAD = head;
        this.LEFT_ARM = leftArm;
        this.LEFT_LEG = leftLeg;
        this.LEFT_FOOT = leftFoot;
        this.BODY = body;
        this.RIGHT_ARM = rightArm;
        this.RIGHT_LEG = rightLeg;
        this.RIGHT_FOOT = rightFoot;
        this.isTemp = isTemp;
    }

    public AbstractDamageablePart getFromEnum(EnumPlayerPart part) {
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

    /**
     * Updates the part.
     * Should not be called by other mods!
     */
    public abstract void tick(World world, EntityPlayer player);

    /**
     * Applies morphine effects
     */
    public abstract void applyMorphine();

    public abstract int getMorphineTicks();

    public abstract float getCurrentHealth();

    /**
     * Checks if the player is dead.
     * This does not mean that the player cannot be revived.
     *
     * @param player The player to check. If null, it will not be checked if the player can be revived (Using PlayerRevival)
     * @return true if dead, false otherwise
     */
    public abstract boolean isDead(@Nullable EntityPlayer player);

    public abstract Float getAbsorption();

    public abstract void setAbsorption(float absorption);

    @SideOnly(Side.CLIENT)
    public abstract int getMaxRenderSize();
}
