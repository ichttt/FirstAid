package ichttt.mods.firstaid.api;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractPlayerDamageModel implements Iterable<AbstractDamageablePart>, INBTSerializable<NBTTagCompound> {
    public final AbstractDamageablePart HEAD, LEFT_ARM, LEFT_LEG, LEFT_FOOT, BODY, RIGHT_ARM, RIGHT_LEG, RIGHT_FOOT;
    public boolean hasTutorial;

    public AbstractPlayerDamageModel(AbstractDamageablePart head, AbstractDamageablePart leftArm, AbstractDamageablePart leftLeg, AbstractDamageablePart leftFoot, AbstractDamageablePart body, AbstractDamageablePart rightArm, AbstractDamageablePart rightLeg, AbstractDamageablePart rightFoot) {
        this.HEAD = head;
        this.LEFT_ARM = leftArm;
        this.LEFT_LEG = leftLeg;
        this.LEFT_FOOT = leftFoot;
        this.BODY = body;
        this.RIGHT_ARM = rightArm;
        this.RIGHT_LEG = rightLeg;
        this.RIGHT_FOOT = rightFoot;
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

    public abstract void tick(World world, EntityPlayer player);

    public abstract void applyMorphine();

    public abstract int getMorphineTicks();

    public abstract float getCurrentHealth();

    public abstract boolean isDead();

    public abstract Float getAbsorption();

    public abstract void setAbsorption(float absorption);

    @SideOnly(Side.CLIENT)
    public abstract int getMaxRenderSize();
}
