package ichttt.mods.firstaid.damagesystem;

import ichttt.mods.firstaid.EventHandler;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.damagesystem.debuff.AbstractDebuff;
import ichttt.mods.firstaid.damagesystem.debuff.Debuffs;
import ichttt.mods.firstaid.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.damagesystem.enums.EnumPlayerPart;
import ichttt.mods.firstaid.util.DataManagerWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;

public class PlayerDamageModel implements INBTSerializable<NBTTagCompound>, Iterable<DamageablePart> {
    public final DamageablePart HEAD, LEFT_ARM, LEFT_LEG, LEFT_FOOT, BODY, RIGHT_ARM, RIGHT_LEG, RIGHT_FOOT;
    private int morphineTicksLeft = 0;
    private float prevHealthCurrent = -1F;
    private float prevScaleFactor;
    private final List<SharedDebuff> sharedDebuffs = new ArrayList<>(2);
    public boolean hasTutorial;

    public static PlayerDamageModel create() {
        return new PlayerDamageModel(Objects.requireNonNull(FirstAid.activeDamageConfig));
    }

    public static PlayerDamageModel createTemp() {
        return new PlayerDamageModel(FirstAidConfig.damageSystem);
    }

    protected PlayerDamageModel(FirstAidConfig.DamageSystem config) {
        AbstractDebuff[] headDebuffs = Debuffs.getHeadDebuffs();
        AbstractDebuff[] bodyDebuffs = Debuffs.getBodyDebuffs();
        SharedDebuff armDebuff = Debuffs.getArmDebuffs();
        SharedDebuff legFootDebuff = Debuffs.getLegFootDebuffs();
        sharedDebuffs.add(armDebuff);
        sharedDebuffs.add(legFootDebuff);
        this.HEAD       = new DamageablePart(config.maxHealthHead,      true,  EnumPlayerPart.HEAD,       headDebuffs  );
        this.LEFT_ARM   = new DamageablePart(config.maxHealthLeftArm,   false, EnumPlayerPart.LEFT_ARM,   armDebuff    );
        this.LEFT_LEG   = new DamageablePart(config.maxHealthLeftLeg,   false, EnumPlayerPart.LEFT_LEG,   legFootDebuff);
        this.LEFT_FOOT  = new DamageablePart(config.maxHealthLeftFoot,  false, EnumPlayerPart.LEFT_FOOT,  legFootDebuff);
        this.BODY       = new DamageablePart(config.maxHealthBody,      true,  EnumPlayerPart.BODY,       bodyDebuffs  );
        this.RIGHT_ARM  = new DamageablePart(config.maxHealthRightArm,  false, EnumPlayerPart.RIGHT_ARM,  armDebuff    );
        this.RIGHT_LEG  = new DamageablePart(config.maxHealthRightLeg,  false, EnumPlayerPart.RIGHT_LEG,  legFootDebuff);
        this.RIGHT_FOOT = new DamageablePart(config.maxHealthRightFoot, false, EnumPlayerPart.RIGHT_FOOT, legFootDebuff);
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
        tagCompound.setBoolean("hasTutorial", hasTutorial);
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
        if (nbt.hasKey("hasTutorial"))
            hasTutorial = nbt.getBoolean("hasTutorial");
    }

    private static void deserializeNBT_legacy(NBTTagCompound nbt, String key, DamageablePart part) {
        part.currentHealth = Math.min(nbt.getFloat(key + "Health"), part.getMaxHealth());
    }

    public void tick(World world, EntityPlayer player) {
        if (player.isDead || player.getHealth() <= 0F)
            return;

        float currentHealth = getCurrentHealth();
        if (currentHealth == 0)
            currentHealth = Float.MIN_VALUE;
        float newCurrentHealth = player.getMaxHealth() * (currentHealth / (FirstAid.scaleMaxHealth ? player.getMaxHealth() : FirstAid.playerMaxHealth));
        if (newCurrentHealth != prevHealthCurrent && !world.isRemote)
            ((DataManagerWrapper) player.dataManager).set_impl(EntityPlayer.HEALTH, newCurrentHealth);
        prevHealthCurrent = newCurrentHealth;

        if (!this.hasTutorial)
            this.hasTutorial = PlayerDataManager.tutorialDone.contains(player.getName());

        if (FirstAid.scaleMaxHealth) {
            float globalFactor = player.getMaxHealth() / 20F;
            if (prevScaleFactor != globalFactor) {
                boolean reduce = false;
                for (DamageablePart part : this) {
                    int result = Math.round(part.initialMaxHealth * globalFactor);
                    if (result % 2 == 1) {
                        if (reduce) {
                            result--;
                            reduce = false;
                        } else {
                            result++;
                            reduce = true;
                        }
                    }
                    part.setMaxHealth(result);
                }
            }
            prevScaleFactor = globalFactor;
        }

        forEach(part -> part.tick(world, player, morphineTicksLeft == 0));
        if (morphineTicksLeft <= 0)
            sharedDebuffs.forEach(sharedDebuff -> sharedDebuff.tick(player));
        else
            morphineTicksLeft--;
    }

    public void applyMorphine() {
        morphineTicksLeft = (EventHandler.rand.nextInt(2) * 20 * 45) + 20 * 210;
    }

    public int getMorphineTicks() {
        return morphineTicksLeft;
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
                if (count > 8)
                    throw new NoSuchElementException();
                DamageablePart part = getFromEnum(EnumPlayerPart.fromID(count));
                count++;
                return part;
            }
        };
    }

    public float getCurrentHealth() {
        float currentHealth = 0;
        for (DamageablePart part : this)
            currentHealth += part.currentHealth;
        return currentHealth;
    }

    public boolean isDead() {
        for (DamageablePart part : this) {
            if (part.canCauseDeath && part.currentHealth <= 0)
                return true;
        }
        return false;
    }

    public Float getAbsorption() {
        float value = 0;
        for (DamageablePart part : this)
                value += part.getAbsorption();
        return value;
    }

    public void setAbsorption(float absorption) {
        float newAbsorption = Math.min(4F, absorption / 8F);
        forEach(damageablePart -> damageablePart.setAbsorption(newAbsorption));
    }

    @SideOnly(Side.CLIENT)
    public int getMaxRenderSize() {
        int max = 0;
        for (DamageablePart part : this)
            max = Math.max(max, (int) (part.getMaxHealth() + part.getAbsorption() + 0.9999F));
        return (int) (((max + 1) / 2F) * 9);
    }
}
