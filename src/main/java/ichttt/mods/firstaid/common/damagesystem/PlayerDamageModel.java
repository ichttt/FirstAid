/*
 * FirstAid
 * Copyright (C) 2017-2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.common.damagesystem;

import com.creativemd.playerrevive.api.IRevival;
import com.google.common.collect.Lists;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.DataManagerWrapper;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class PlayerDamageModel extends AbstractPlayerDamageModel {
    private final Set<SharedDebuff> sharedDebuffs = new HashSet<>();
    private int morphineTicksLeft = 0;
    private int sleepBlockTicks = 0;
    private float prevHealthCurrent = -1F;
    private float prevScaleFactor;
    private boolean waitingForHelp = false;
    private final boolean noCritical;
    private boolean needsMorphineUpdate = false;
    private int resyncTimer = -1;
    private FirstAidConfig.VanillaMaxHealthMode prevMode = null;

    public static PlayerDamageModel create() {
        FirstAidRegistry registry = FirstAidRegistryImpl.INSTANCE;
        IDebuff[] headDebuffs = registry.getDebuffs(EnumDebuffSlot.HEAD);
        IDebuff[] bodyDebuffs = registry.getDebuffs(EnumDebuffSlot.BODY);
        IDebuff[] armsDebuffs = registry.getDebuffs(EnumDebuffSlot.ARMS);
        IDebuff[] legFootDebuffs = registry.getDebuffs(EnumDebuffSlot.LEGS_AND_FEET);
        return new PlayerDamageModel(headDebuffs, bodyDebuffs, armsDebuffs, legFootDebuffs);
    }

    protected PlayerDamageModel(IDebuff[] headDebuffs, IDebuff[] bodyDebuffs, IDebuff[] armDebuffs, IDebuff[] legFootDebuffs) {
        super(new DamageablePart(FirstAidConfig.damageSystem.maxHealthHead,      FirstAidConfig.damageSystem.causeDeathHead,  EnumPlayerPart.HEAD,       headDebuffs   ),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftArm,   false,                         EnumPlayerPart.LEFT_ARM,   armDebuffs    ),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftLeg,   false,                         EnumPlayerPart.LEFT_LEG,   legFootDebuffs),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthLeftFoot,  false,                         EnumPlayerPart.LEFT_FOOT,  legFootDebuffs),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthBody,      FirstAidConfig.damageSystem.causeDeathBody,  EnumPlayerPart.BODY,       bodyDebuffs   ),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightArm,  false,                         EnumPlayerPart.RIGHT_ARM,  armDebuffs    ),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightLeg,  false,                         EnumPlayerPart.RIGHT_LEG,  legFootDebuffs),
              new DamageablePart(FirstAidConfig.damageSystem.maxHealthRightFoot, false,                         EnumPlayerPart.RIGHT_FOOT, legFootDebuffs));
        for (IDebuff debuff : armDebuffs)
            this.sharedDebuffs.add((SharedDebuff) debuff);
        for (IDebuff debuff : legFootDebuffs)
            this.sharedDebuffs.add((SharedDebuff) debuff);
        noCritical = !FirstAidConfig.damageSystem.causeDeathBody && !FirstAidConfig.damageSystem.causeDeathHead;
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
        tagCompound.setBoolean("hasTutorial", hasTutorial);
        tagCompound.setInteger("maxHealthMode", FirstAidConfig.maxHealthMode.ordinal());
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        HEAD.deserializeNBT((NBTTagCompound) nbt.getTag("head"));
        LEFT_ARM.deserializeNBT((NBTTagCompound) nbt.getTag("leftArm"));
        LEFT_LEG.deserializeNBT((NBTTagCompound) nbt.getTag("leftLeg"));
        LEFT_FOOT.deserializeNBT((NBTTagCompound) nbt.getTag("leftFoot"));
        BODY.deserializeNBT((NBTTagCompound) nbt.getTag("body"));
        RIGHT_ARM.deserializeNBT((NBTTagCompound) nbt.getTag("rightArm"));
        RIGHT_LEG.deserializeNBT((NBTTagCompound) nbt.getTag("rightLeg"));
        RIGHT_FOOT.deserializeNBT((NBTTagCompound) nbt.getTag("rightFoot"));
        if (nbt.hasKey("morphineTicks")) { //legacy - we still have to write it
            morphineTicksLeft = nbt.getInteger("morphineTicks");
            needsMorphineUpdate = true;
        }
        if (nbt.hasKey("hasTutorial"))
            hasTutorial = nbt.getBoolean("hasTutorial");
        if (nbt.hasKey("maxHealthMode")) {
            int maxHealthMode = nbt.getInteger("maxHealthMode");
            if (maxHealthMode != FirstAidConfig.maxHealthMode.ordinal()) {
                prevMode = FirstAidConfig.VanillaMaxHealthMode.values()[maxHealthMode];
            }
        } else {
            // Workaround for old versions: IF a player part saved its max health the old mode was SCALE, and IGNORE otherwise
            boolean hadStoredMaxHealth = nbt.getCompoundTag("head").hasKey("maxHealth");
            FirstAidConfig.VanillaMaxHealthMode oldMode = hadStoredMaxHealth ? FirstAidConfig.VanillaMaxHealthMode.SCALE_FIRSTAID_TO_FIT_VANILLA : FirstAidConfig.VanillaMaxHealthMode.IGNORE;
            if (oldMode != FirstAidConfig.maxHealthMode) {
                prevMode = oldMode;
            }
        }
    }

    @Override
    public void tick(World world, EntityPlayer player) {
        if (isDead(player))
            return;
        world.profiler.startSection("FirstAidPlayerModel");
        if (sleepBlockTicks > 0)
            sleepBlockTicks--;
        else if (sleepBlockTicks < 0)
            throw new RuntimeException("Negative sleepBlockTicks " + sleepBlockTicks);
        if (prevMode != null) {
            if (!world.isRemote && prevMode != FirstAidConfig.maxHealthMode) {
                if (prevMode == FirstAidConfig.VanillaMaxHealthMode.SYNC_FIRSTAID_VANILLA) {
                    // sync -> none/scaled transition
                    // We have to reset to 20.0 plus whatever other mods granted/reduced
                    float newMaxHealth = player.getMaxHealth() - FirstAidConfig.damageSystem.getTotalMaxHealth() + 20F;
                    player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(newMaxHealth);
                    scheduleResync();
                } else if (FirstAidConfig.maxHealthMode == FirstAidConfig.VanillaMaxHealthMode.SYNC_FIRSTAID_VANILLA) {
                    // none/scaled -> sync transition
                    // We have to add so we match the max total health plus whatever other mods granted/reduced
                    float newMaxHealth = player.getMaxHealth() + FirstAidConfig.damageSystem.getTotalMaxHealth() - 20F;
                    player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(newMaxHealth);
                    scheduleResync();
                }
            }
            prevMode = null;
        }

        float newCurrentHealth = calculateNewCurrentHealth(player);
        if (Float.isNaN(newCurrentHealth)) {
            FirstAid.LOGGER.warn("New current health is not a number, setting it to 0!");
            newCurrentHealth = 0F;
        }
        if (newCurrentHealth <= 0F) {
            FirstAid.LOGGER.error("Got {} health left, but isn't marked as dead!", newCurrentHealth);
            world.profiler.endSection();
            return;
        }
        if (!world.isRemote && resyncTimer != -1) {
            resyncTimer--;
            if (resyncTimer == 0) {
                resyncTimer = -1;
                FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(this, true), (EntityPlayerMP) player);
            }
        }

        if (Float.isInfinite(newCurrentHealth)) {
            FirstAid.LOGGER.error("Error calculating current health: Value was infinite"); //Shouldn't happen anymore, but let's be safe
        } else {
            if (newCurrentHealth != prevHealthCurrent)
                ((DataManagerWrapper) player.dataManager).set_impl(EntityPlayer.HEALTH, newCurrentHealth);
            prevHealthCurrent = newCurrentHealth;
        }

        if (!this.hasTutorial)
            this.hasTutorial = CapProvider.tutorialDone.contains(player.getName());

        runScaleLogic(player);

        //morphine update
        if (this.needsMorphineUpdate) {
            player.addPotionEffect(new PotionEffect(EventHandler.MORPHINE, this.morphineTicksLeft, 0, false, false));
        }
        PotionEffect morphine = player.getActivePotionEffect(EventHandler.MORPHINE);
        if (!this.needsMorphineUpdate) {
            this.morphineTicksLeft = morphine == null ? 0 : morphine.getDuration();
        }
        this.needsMorphineUpdate = false;

        //Debuff and part ticking
        world.profiler.startSection("PartDebuffs");
        forEach(part -> part.tick(world, player, morphine == null));
        if (morphine == null)
            sharedDebuffs.forEach(sharedDebuff -> sharedDebuff.tick(player));
        world.profiler.endSection();
        world.profiler.endSection();
    }

    public static int getRandMorphineDuration() { //Tweak tooltip event when changing as well
        return ((EventHandler.rand.nextInt(5) * 20 * 15) + 20 * 210);
    }

    @Deprecated
    @Override
    public void applyMorphine() {
        morphineTicksLeft = getRandMorphineDuration();
        needsMorphineUpdate = true;
    }

    @Override
    public void applyMorphine(EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(EventHandler.MORPHINE, getRandMorphineDuration(), 0, false, false));
    }

    @Deprecated
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

    @Deprecated
    @Override
    public float getCurrentHealth() {
        float currentHealth = 0;
        for (AbstractDamageablePart part : this)
            currentHealth += part.currentHealth;
        return currentHealth;
    }

    private float calculateNewCurrentHealth(EntityPlayer player) {
        float currentHealth = 0;
        FirstAidConfig.VanillaHealthCalculationMode mode = FirstAidConfig.vanillaHealthCalculation;
        if (noCritical) mode = FirstAidConfig.VanillaHealthCalculationMode.AVERAGE_ALL;
        switch (mode) {
            case AVERAGE_CRITICAL:
                int maxHealth = 0;
                for (AbstractDamageablePart part : this) {
                    if (part.canCauseDeath) {
                        currentHealth += part.currentHealth;
                        maxHealth += part.getMaxHealth();
                    }
                }
                currentHealth = currentHealth / maxHealth;
                break;
            case MIN_CRITICAL:
                AbstractDamageablePart minimal = null;
                float lowest = Float.MAX_VALUE;
                for (AbstractDamageablePart part : this) {
                    if (part.canCauseDeath) {
                        float partCurrentHealth = part.currentHealth;
                        if (partCurrentHealth < lowest) {
                            minimal = part;
                            lowest = partCurrentHealth;
                        }
                    }
                }
                Objects.requireNonNull(minimal);
                currentHealth = minimal.currentHealth / minimal.getMaxHealth();
                break;
            case AVERAGE_ALL:
                for (AbstractDamageablePart part : this)
                    currentHealth += part.currentHealth;
                currentHealth = currentHealth / getCurrentMaxHealth();
                break;
            case CRITICAL_50_PERCENT_OTHER_50_PERCENT:
                float currentNormal = 0;
                int maxNormal = 0;
                float currentCritical = 0;
                int maxCritical = 0;
                for (AbstractDamageablePart part : this) {
                    if (!part.canCauseDeath) {
                        currentNormal += part.currentHealth;
                        maxNormal += part.getMaxHealth();
                    } else {
                        currentCritical += part.currentHealth;
                        maxCritical += part.getMaxHealth();
                    }
                }
                float avgNormal = currentNormal / maxNormal;
                float avgCritical = currentCritical / maxCritical;
                currentHealth = (avgCritical + avgNormal) / 2;
                break;
            default:
                throw new RuntimeException("Unknown constant " + mode);
        }
        return currentHealth * player.getMaxHealth();
    }

    @Override
    public boolean isDead(@Nullable EntityPlayer player) {
        IRevival revival = CommonUtils.getRevivalIfPossible(player);
        if (revival != null) {
            if (!revival.isHealty() && !revival.isDead()) {
                if (FirstAidConfig.debug && !waitingForHelp)
                    FirstAid.LOGGER.info("Player start waiting for help");
                this.waitingForHelp = true; //Technically not dead yet, but we should still return true
                return true;
            } else if (this.waitingForHelp) {
                return true;
            }
        }

        if (player != null && (player.isDead || player.getHealth() <= 0F))
            return true;

        if (this.noCritical) {
            boolean dead = true;
            for (AbstractDamageablePart part : this) {
                if (part.currentHealth > 0) {
                    dead = false;
                    break;
                }
            }
            return dead;
        } else {
            for (AbstractDamageablePart part : this) {
                if (part.canCauseDeath && part.currentHealth <= 0) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Float getAbsorption() { //Float class because of DataManager
        float value = 0;
        for (AbstractDamageablePart part : this)
                value += part.getAbsorption();
        return value; //Autoboxing FTW
    }

    @Override
    public void setAbsorption(float absorption) {
        final float newAbsorption = absorption / 8F;
        forEach(damageablePart -> damageablePart.setAbsorption(newAbsorption));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getMaxRenderSize() {
        int max = 0;
        for (AbstractDamageablePart part : this) {
            int newMax;
            if (FirstAidConfig.overlay.overlayMode == FirstAidConfig.Overlay.OverlayMode.NUMBERS)
                newMax = Minecraft.getMinecraft().fontRenderer.getStringWidth(HealthRenderUtils.TEXT_FORMAT.format(part.currentHealth) + "/" + part.getMaxHealth()) + 1;
            else
                newMax = (int) (((((int) (part.getMaxHealth() + part.getAbsorption() + 0.9999F)) + 1) / 2F) * 9F);
            max = Math.max(max, newMax);
        }
        return max;
    }

    @Override
    public void sleepHeal(EntityPlayer player) {
        if (sleepBlockTicks > 0)
            return;
        CommonUtils.healPlayerByPercentage(FirstAidConfig.externalHealing.sleepHealPercentage, this, player);
        sleepBlockTicks = 20;
    }

    @Override
    public int getCurrentMaxHealth() {
        int maxHealth = 0;
        for (AbstractDamageablePart part : this) {
            maxHealth += part.getMaxHealth();
        }
        return maxHealth;
    }

    @Override
    public void stopWaitingForHelp(EntityPlayer player) {
        if (FirstAidConfig.debug) {
            FirstAid.LOGGER.info("Help waiting done!");
        }
        if (!this.waitingForHelp)
            FirstAid.LOGGER.warn("Player {} not waiting for help!", player.getName());
        this.waitingForHelp = false;
    }

    @Override
    public boolean isWaitingForHelp() {
        return this.waitingForHelp;
    }

    @Deprecated
    @Override
    public void onNotHelped(EntityPlayer player) {
        stopWaitingForHelp(player);
    }

    @Deprecated
    @Override
    public void onHelpedUp(EntityPlayer player) {
        stopWaitingForHelp(player);
        revivePlayer(player);
    }

    @Override
    public void revivePlayer(EntityPlayer player) {
        if (FirstAidConfig.debug) {
            CommonUtils.debugLogStacktrace("Reviving player");
        }
        player.isDead = false;
        for (AbstractDamageablePart part : this) {
            if ((part.canCauseDeath || this.noCritical) && part.currentHealth <= 0F) {
                part.currentHealth = 1F; // Set the critical health to a non-zero value
            }
        }
        //make sure to resync the client health
        if (!player.world.isRemote && player instanceof EntityPlayerMP)
            FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(this, true), (EntityPlayerMP) player); //Upload changes to the client
    }

    @Override
    public void runScaleLogic(EntityPlayer player) {
        if (FirstAidConfig.maxHealthMode == FirstAidConfig.VanillaMaxHealthMode.SCALE_FIRSTAID_TO_FIT_VANILLA) { //Attempt to calculate the max health of the body parts based on the maxHealth attribute
            player.world.profiler.startSection("healthscaling");
            float globalFactor = player.getMaxHealth() / 20F;
            if (prevScaleFactor != globalFactor) {
                if (FirstAidConfig.debug) {
                    FirstAid.LOGGER.info("Starting health scaling factor {} -> {} (max health {})", prevScaleFactor, globalFactor, player.getMaxHealth());
                }
                player.world.profiler.startSection("distribution");
                int reduced = 0;
                int added = 0;
                float expectedNewMaxHealth = 0F;
                int newMaxHealth = 0;
                for (AbstractDamageablePart part : this) {
                    float floatResult = ((float) part.initialMaxHealth) * globalFactor;
                    expectedNewMaxHealth += floatResult;
                    int result = (int) floatResult;
                    if (result % 2 == 1) {
                        int partMaxHealth = part.getMaxHealth();
                        if (part.currentHealth < partMaxHealth && reduced < 4) {
                            result--;
                            reduced++;
                        } else if (part.currentHealth > partMaxHealth && added < 4) {
                            result++;
                            added++;
                        } else if (reduced > added) {
                            result++;
                            added++;
                        } else {
                            result--;
                            reduced++;
                        }
                    }
                    newMaxHealth += result;
                    if (FirstAidConfig.debug) {
                        FirstAid.LOGGER.info("Part {} max health: {} initial; {} old; {} new", part.part.name(), part.initialMaxHealth, part.getMaxHealth(), result);
                    }
                    part.setMaxHealth(result);
                }
                player.world.profiler.endStartSection("correcting");
                if (Math.abs(expectedNewMaxHealth - newMaxHealth) >= 2F) {
                    if (FirstAidConfig.debug) {
                        FirstAid.LOGGER.info("Entering second stage - diff {}", Math.abs(expectedNewMaxHealth - newMaxHealth));
                    }
                    List<AbstractDamageablePart> prioList = new ArrayList<>();
                    for (AbstractDamageablePart part : this) {
                        prioList.add(part);
                    }
                    prioList.sort(Comparator.comparingInt(AbstractDamageablePart::getMaxHealth));
                    for (AbstractDamageablePart part : prioList) {
                        int maxHealth = part.getMaxHealth();
                        if (FirstAidConfig.debug) {
                            FirstAid.LOGGER.info("Part {}: Second stage with total diff {}", part.part.name(), Math.abs(expectedNewMaxHealth - newMaxHealth));
                        }
                        if (expectedNewMaxHealth > newMaxHealth) {
                            part.setMaxHealth(maxHealth + 2);
                            newMaxHealth += (part.getMaxHealth() - maxHealth);
                        } else if (expectedNewMaxHealth < newMaxHealth) {
                            part.setMaxHealth(maxHealth - 2);
                            newMaxHealth -= (maxHealth - part.getMaxHealth());
                        }
                        if (Math.abs(expectedNewMaxHealth - newMaxHealth) < 2F) {
                            break;
                        }
                    }
                }
                player.world.profiler.endSection();
            }
            prevScaleFactor = globalFactor;
            player.world.profiler.endSection();
        } else if (FirstAidConfig.maxHealthMode == FirstAidConfig.VanillaMaxHealthMode.SYNC_FIRSTAID_VANILLA) {
            player.world.profiler.startSection("healthscaling");
            float floatNewMaxHealth = player.getMaxHealth();
            if (prevScaleFactor != floatNewMaxHealth) {
                if (FirstAidConfig.debug) {
                    FirstAid.LOGGER.info("Starting health scaling max health {} -> {}", prevScaleFactor, floatNewMaxHealth);
                }
                player.world.profiler.startSection("distribution");
                int initialMaxHealth = FirstAidConfig.damageSystem.getTotalMaxHealth();
                int newMaxHealth = Math.round(floatNewMaxHealth);
                int diff = newMaxHealth - initialMaxHealth;
                List<AbstractDamageablePart> parts = new ArrayList<>();
                for (EnumPlayerPart value : EnumPlayerPart.VALUES) {
                    parts.add(getFromEnum(value));
                }
                if (diff > 0) {
                    parts.sort((o1, o2) -> Integer.compare(o1.initialMaxHealth, o2.initialMaxHealth));
                    int remaining = EnumPlayerPart.VALUES.length;
                    for (AbstractDamageablePart part : parts) {
                        // Make sure to always add 2, as that's the lowest we can do
                        int toAdd = MathHelper.roundUp(MathHelper.ceil(diff / (float) remaining), 2);
                        // Nothing left to add, return
                        if (toAdd <= 0) toAdd = 0;
                        // We want to add more than we are allowed to, reduce toAdd by two until we fit
                        // TODO can this even happen?
                        while (toAdd > diff) {
                            toAdd -= 2;
                            // Doesn't fit, break
                            if (toAdd <= 2) {
                                toAdd = 0;
                                break;
                            }
                        }
                        int toSet = part.initialMaxHealth + toAdd;
                        if (FirstAidConfig.debug) {
                            FirstAid.LOGGER.info("Part {} max health: {} initial; {} old; {} new", part.part.name(), part.initialMaxHealth, part.getMaxHealth(), toSet);
                        }
                        part.setMaxHealth(toSet);
                        // Make sure we have not reached the limit yet
                        if (part.getMaxHealth() == toSet) {
                            diff -= toAdd;
                        }
                        remaining--;
                    }
                } else if (diff < 0) {
                    diff = -diff; // Flip the value, so we know what to subtract
                    parts.sort((o1, o2) -> Integer.compare(o2.initialMaxHealth, o1.initialMaxHealth));
                    int remaining = EnumPlayerPart.VALUES.length;
                    for (AbstractDamageablePart part : parts) {
                        // Make sure to always sub 2, as that's the lowest we can do
                        int toSub = MathHelper.roundUp(MathHelper.ceil(diff / (float) remaining), 2);
                        // Nothing left to add, return
                        if (toSub <= 0) toSub = 0;
                        // We want to add more than we are allowed to, reduce toAdd by two until we fit
                        // TODO can this even happen?
                        while (toSub > diff) {
                            toSub -= 2;
                            // Doesn't fit, break
                            if (toSub <= 2) {
                                toSub = 0;
                                break;
                            }
                        }
                        int toSet = part.initialMaxHealth - toSub;
                        if (FirstAidConfig.debug) {
                            FirstAid.LOGGER.info("Part {} max health: {} initial; {} old; {} new", part.part.name(), part.initialMaxHealth, part.getMaxHealth(), toSet);
                        }
                        part.setMaxHealth(toSet);
                        // Make sure we have not reached the limit yet
                        if (part.getMaxHealth() == toSet) {
                            diff -= toSub;
                        }
                        remaining--;
                    }
                } else {
                    for (AbstractDamageablePart part : parts) {
                        part.setMaxHealth(part.initialMaxHealth);
                    }
                }
                player.world.profiler.endSection();
            }
            prevScaleFactor = floatNewMaxHealth;
            player.world.profiler.endSection();
        }
    }

    @Override
    public void scheduleResync() {
        if (this.resyncTimer == -1) {
            this.resyncTimer = 3;
        } else {
            FirstAid.LOGGER.warn("resync already scheduled!");
        }
    }

    @Override
    public boolean hasNoCritical() {
        return this.noCritical;
    }
}
