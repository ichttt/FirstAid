package ichttt.mods.firstaid.damagesystem;

import ichttt.mods.firstaid.EventHandler;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.AbstractDamageablePart;
import ichttt.mods.firstaid.api.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.damagesystem.debuff.AbstractDebuff;
import ichttt.mods.firstaid.damagesystem.debuff.Debuffs;
import ichttt.mods.firstaid.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.util.DataManagerWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;

public class PlayerDamageModel extends AbstractPlayerDamageModel {
    private int morphineTicksLeft = 0;
    private float prevHealthCurrent = -1F;
    private float prevScaleFactor;
    private final List<SharedDebuff> sharedDebuffs = new ArrayList<>(2);

    public static AbstractPlayerDamageModel create() {
        return create_impl(Objects.requireNonNull(FirstAid.activeDamageConfig));
    }

    public static AbstractPlayerDamageModel createTemp() {
        return create_impl(FirstAidConfig.damageSystem);
    }

    private static AbstractPlayerDamageModel create_impl(FirstAidConfig.DamageSystem config) {
        AbstractDebuff[] headDebuffs = Debuffs.getHeadDebuffs();
        AbstractDebuff[] bodyDebuffs = Debuffs.getBodyDebuffs();
        SharedDebuff armDebuff = Debuffs.getArmDebuffs();
        SharedDebuff legFootDebuff = Debuffs.getLegFootDebuffs();
        return new PlayerDamageModel(config, headDebuffs, bodyDebuffs, armDebuff, legFootDebuff);
    }


    protected PlayerDamageModel(FirstAidConfig.DamageSystem config, AbstractDebuff[] headDebuffs, AbstractDebuff[] bodyDebuffs, SharedDebuff armDebuff, SharedDebuff legFootDebuff) {
        super(new DamageablePart(config.maxHealthHead,      true,  EnumPlayerPart.HEAD,       headDebuffs  ),
              new DamageablePart(config.maxHealthLeftArm,   false, EnumPlayerPart.LEFT_ARM,   armDebuff    ),
              new DamageablePart(config.maxHealthLeftLeg,   false, EnumPlayerPart.LEFT_LEG,   legFootDebuff),
              new DamageablePart(config.maxHealthLeftFoot,  false, EnumPlayerPart.LEFT_FOOT,  legFootDebuff),
              new DamageablePart(config.maxHealthBody,      true,  EnumPlayerPart.BODY,       bodyDebuffs  ),
              new DamageablePart(config.maxHealthRightArm,  false, EnumPlayerPart.RIGHT_ARM,  armDebuff    ),
              new DamageablePart(config.maxHealthRightLeg,  false, EnumPlayerPart.RIGHT_LEG,  legFootDebuff),
              new DamageablePart(config.maxHealthRightFoot, false, EnumPlayerPart.RIGHT_FOOT, legFootDebuff));
        sharedDebuffs.add(armDebuff);
        sharedDebuffs.add(legFootDebuff);
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

    private static void deserializeNBT_legacy(NBTTagCompound nbt, String key, AbstractDamageablePart part) {
        part.currentHealth = Math.min(nbt.getFloat(key + "Health"), part.getMaxHealth());
    }

    @Override
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
                for (AbstractDamageablePart part : this) {
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

    @Override
    public void applyMorphine() {
        morphineTicksLeft = (EventHandler.rand.nextInt(2) * 20 * 45) + 20 * 210;
    }

    @Override
    public int getMorphineTicks() {
        return morphineTicksLeft;
    }

    @Override
    @Nonnull
    public Iterator<AbstractDamageablePart> iterator() {
        return new Iterator<AbstractDamageablePart>() {
            byte count = 1;
            @Override
            public boolean hasNext() {
                return count <= 8;
            }

            @Override
            public AbstractDamageablePart next() {
                if (count > 8)
                    throw new NoSuchElementException();
                AbstractDamageablePart part = getFromEnum(EnumPlayerPart.fromID(count));
                count++;
                return part;
            }
        };
    }

    @Override
    public float getCurrentHealth() {
        float currentHealth = 0;
        for (AbstractDamageablePart part : this)
            currentHealth += part.currentHealth;
        return currentHealth;
    }

    @Override
    public boolean isDead() {
        for (AbstractDamageablePart part : this) {
            if (part.canCauseDeath && part.currentHealth <= 0)
                return true;
        }
        return false;
    }

    @Override
    public Float getAbsorption() {
        float value = 0;
        for (AbstractDamageablePart part : this)
                value += part.getAbsorption();
        return value;
    }

    @Override
    public void setAbsorption(float absorption) {
        float newAbsorption = Math.min(4F, absorption / 8F);
        forEach(damageablePart -> damageablePart.setAbsorption(newAbsorption));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getMaxRenderSize() {
        int max = 0;
        for (AbstractDamageablePart part : this)
            max = Math.max(max, (int) (part.getMaxHealth() + part.getAbsorption() + 0.9999F));
        return (int) (((max + 1) / 2F) * 9);
    }
}
