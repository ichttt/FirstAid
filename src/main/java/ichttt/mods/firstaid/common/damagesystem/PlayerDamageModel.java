package ichttt.mods.firstaid.common.damagesystem;

import com.creativemd.playerrevive.api.IRevival;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.FirstAidConfig;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.capability.PlayerDataManager;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.network.MessageAddHealth;
import ichttt.mods.firstaid.common.network.MessageResync;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.DataManagerWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class PlayerDamageModel extends AbstractPlayerDamageModel {
    private int morphineTicksLeft = 0;
    private float prevHealthCurrent = -1F;
    private float prevScaleFactor;
    private final Set<SharedDebuff> sharedDebuffs = new HashSet<>();
    private boolean waitingForHelp = false;

    public static PlayerDamageModel create() {
        return create_impl(Objects.requireNonNull(FirstAid.activeDamageConfig));
    }

    public static PlayerDamageModel createTemp() {
        return create_impl(FirstAidConfig.damageSystem);
    }

    private static PlayerDamageModel create_impl(FirstAidConfig.DamageSystem config) {
        FirstAidRegistry registry = FirstAidRegistryImpl.INSTANCE;
        IDebuff[] headDebuffs = registry.getDebuffs(EnumDebuffSlot.HEAD);
        IDebuff[] bodyDebuffs = registry.getDebuffs(EnumDebuffSlot.BODY);
        IDebuff[] armsDebuffs = registry.getDebuffs(EnumDebuffSlot.ARMS);
        IDebuff[] legFootDebuffs = registry.getDebuffs(EnumDebuffSlot.LEGS_AND_FEET);
        return new PlayerDamageModel(config, headDebuffs, bodyDebuffs, armsDebuffs, legFootDebuffs);
    }

    protected PlayerDamageModel(FirstAidConfig.DamageSystem config, IDebuff[] headDebuffs, IDebuff[] bodyDebuffs, IDebuff[] armDebuffs, IDebuff[] legFootDebuffs) {
        super(new DamageablePart(config.maxHealthHead,      true,  EnumPlayerPart.HEAD,       headDebuffs   ),
              new DamageablePart(config.maxHealthLeftArm,   false, EnumPlayerPart.LEFT_ARM,   armDebuffs    ),
              new DamageablePart(config.maxHealthLeftLeg,   false, EnumPlayerPart.LEFT_LEG,   legFootDebuffs),
              new DamageablePart(config.maxHealthLeftFoot,  false, EnumPlayerPart.LEFT_FOOT,  legFootDebuffs),
              new DamageablePart(config.maxHealthBody,      true,  EnumPlayerPart.BODY,       bodyDebuffs   ),
              new DamageablePart(config.maxHealthRightArm,  false, EnumPlayerPart.RIGHT_ARM,  armDebuffs    ),
              new DamageablePart(config.maxHealthRightLeg,  false, EnumPlayerPart.RIGHT_LEG,  legFootDebuffs),
              new DamageablePart(config.maxHealthRightFoot, false, EnumPlayerPart.RIGHT_FOOT, legFootDebuffs));
        for (IDebuff debuff : armDebuffs)
            this.sharedDebuffs.add((SharedDebuff) debuff);
        for (IDebuff debuff : legFootDebuffs)
            this.sharedDebuffs.add((SharedDebuff) debuff);
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
        if (isDead(player))
            return;

        float currentHealth = getCurrentHealth();
        if (currentHealth <= 0F) {
            FirstAid.logger.error("Got {} health left, but isn't marked as dead!", currentHealth);
            return;
        }

        if (FirstAid.playerMaxHealth != -1) {
            float newCurrentHealth = player.getMaxHealth() * (currentHealth / (FirstAid.scaleMaxHealth ? player.getMaxHealth() : FirstAid.playerMaxHealth));

            if (Float.isInfinite(newCurrentHealth)) {
                FirstAid.logger.error("Error calculating current health: Value was infinite"); //Shouldn't happen anymore, but let's be safe
            } else {
                if (newCurrentHealth != prevHealthCurrent)
                    ((DataManagerWrapper) player.dataManager).set_impl(EntityPlayer.HEALTH, newCurrentHealth);
                prevHealthCurrent = newCurrentHealth;
            }
        }

        if (!this.hasTutorial)
            this.hasTutorial = PlayerDataManager.tutorialDone.contains(player.getName());

        if (FirstAid.scaleMaxHealth) { //Attempt to calculate the max health of the body parts based on the maxHealth attribute
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

        if (!world.isRemote && world instanceof WorldServer && FirstAid.activeHealingConfig.sleepHealing != 0) {
            WorldServer worldServer = (WorldServer) player.world;
            if (worldServer.areAllPlayersAsleep()) { // We are going to wake up on the next tick, add health TODO morpheus compat?
                HealthDistribution.distributeHealth(FirstAid.activeHealingConfig.sleepHealing, player, true);
            }
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
    public boolean isDead(@Nullable EntityPlayer player) {
        IRevival revival = CommonUtils.getRevivalIfPossible(player);
        if (revival != null) {
            if (!revival.isHealty() && !revival.isDead()) {
                this.waitingForHelp = true; //Technically not dead yet, but we should still return true
                return true;
            } else if (this.waitingForHelp && revival.isRevived()) { //We just got revived
                this.waitingForHelp = false;
                player.isDead = false;
                for (AbstractDamageablePart part : this) {
                    if (part.canCauseDeath && part.currentHealth <= 0F) {
                        part.currentHealth = 1F; // Set the critical health to a non-zero value
                    }
                }
                //make sure to resync the client health
                if (!player.world.isRemote && player instanceof EntityPlayerMP)
                    FirstAid.NETWORKING.sendTo(new MessageResync(this), (EntityPlayerMP) player); //Upload changes to the client
                return false;
            }
        }

        if (player != null && (player.isDead || player.getHealth() <= 0F))
            return true;

        for (AbstractDamageablePart part : this) {
            if (part.canCauseDeath && part.currentHealth <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Float getAbsorption() { //Float class because of DataManager
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
