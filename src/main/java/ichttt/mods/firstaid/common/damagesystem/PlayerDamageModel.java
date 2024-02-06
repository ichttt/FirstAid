/*
 * FirstAid
 * Copyright (C) 2017-2024
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

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.RegistryObjects;
import ichttt.mods.firstaid.common.SynchedEntityDataWrapper;
import ichttt.mods.firstaid.common.compat.playerrevive.PRCompatManager;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.registries.FirstAidRegistryLookups;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PlayerDamageModel extends AbstractPlayerDamageModel {
    private final Set<SharedDebuff> sharedDebuffs = new HashSet<>();
    private int morphineTicksLeft = 0;
    private int sleepBlockTicks = 0;
    private float prevHealthCurrent = -1F;
    private float prevScaleFactor;
    private final boolean noCritical;
    private boolean needsMorphineUpdate = false;
    private int resyncTimer = -1;

    public static PlayerDamageModel create() {
        IDebuff[] headDebuffs = FirstAidRegistryLookups.getDebuffs(EnumDebuffSlot.HEAD);
        IDebuff[] bodyDebuffs = FirstAidRegistryLookups.getDebuffs(EnumDebuffSlot.BODY);
        IDebuff[] armsDebuffs = FirstAidRegistryLookups.getDebuffs(EnumDebuffSlot.ARMS);
        IDebuff[] legFootDebuffs = FirstAidRegistryLookups.getDebuffs(EnumDebuffSlot.LEGS_AND_FEET);
        return new PlayerDamageModel(headDebuffs, bodyDebuffs, armsDebuffs, legFootDebuffs);
    }

    protected PlayerDamageModel(IDebuff[] headDebuffs, IDebuff[] bodyDebuffs, IDebuff[] armDebuffs, IDebuff[] legFootDebuffs) {
        super(new DamageablePart(FirstAidConfig.SERVER.maxHealthHead.get(),      FirstAidConfig.SERVER.causeDeathHead.get(),  EnumPlayerPart.HEAD,       headDebuffs   ),
              new DamageablePart(FirstAidConfig.SERVER.maxHealthLeftArm.get(),   false,                         EnumPlayerPart.LEFT_ARM,   armDebuffs    ),
              new DamageablePart(FirstAidConfig.SERVER.maxHealthLeftLeg.get(),   false,                         EnumPlayerPart.LEFT_LEG,   legFootDebuffs),
              new DamageablePart(FirstAidConfig.SERVER.maxHealthLeftFoot.get(),  false,                         EnumPlayerPart.LEFT_FOOT,  legFootDebuffs),
              new DamageablePart(FirstAidConfig.SERVER.maxHealthBody.get(),      FirstAidConfig.SERVER.causeDeathBody.get(),  EnumPlayerPart.BODY,       bodyDebuffs   ),
              new DamageablePart(FirstAidConfig.SERVER.maxHealthRightArm.get(),  false,                         EnumPlayerPart.RIGHT_ARM,  armDebuffs    ),
              new DamageablePart(FirstAidConfig.SERVER.maxHealthRightLeg.get(),  false,                         EnumPlayerPart.RIGHT_LEG,  legFootDebuffs),
              new DamageablePart(FirstAidConfig.SERVER.maxHealthRightFoot.get(), false,                         EnumPlayerPart.RIGHT_FOOT, legFootDebuffs));
        for (IDebuff debuff : armDebuffs)
            this.sharedDebuffs.add((SharedDebuff) debuff);
        for (IDebuff debuff : legFootDebuffs)
            this.sharedDebuffs.add((SharedDebuff) debuff);
        noCritical = !FirstAidConfig.SERVER.causeDeathBody.get() && !FirstAidConfig.SERVER.causeDeathHead.get();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tagCompound = new CompoundTag();
        tagCompound.put("head", HEAD.serializeNBT());
        tagCompound.put("leftArm", LEFT_ARM.serializeNBT());
        tagCompound.put("leftLeg", LEFT_LEG.serializeNBT());
        tagCompound.put("leftFoot", LEFT_FOOT.serializeNBT());
        tagCompound.put("body", BODY.serializeNBT());
        tagCompound.put("rightArm", RIGHT_ARM.serializeNBT());
        tagCompound.put("rightLeg", RIGHT_LEG.serializeNBT());
        tagCompound.put("rightFoot", RIGHT_FOOT.serializeNBT());
        tagCompound.putBoolean("hasTutorial", hasTutorial);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        HEAD.deserializeNBT((CompoundTag) nbt.get("head"));
        LEFT_ARM.deserializeNBT((CompoundTag) nbt.get("leftArm"));
        LEFT_LEG.deserializeNBT((CompoundTag) nbt.get("leftLeg"));
        LEFT_FOOT.deserializeNBT((CompoundTag) nbt.get("leftFoot"));
        BODY.deserializeNBT((CompoundTag) nbt.get("body"));
        RIGHT_ARM.deserializeNBT((CompoundTag) nbt.get("rightArm"));
        RIGHT_LEG.deserializeNBT((CompoundTag) nbt.get("rightLeg"));
        RIGHT_FOOT.deserializeNBT((CompoundTag) nbt.get("rightFoot"));
        if (nbt.contains("morphineTicks")) { //legacy - we still have to write it
            morphineTicksLeft = nbt.getInt("morphineTicks");
            needsMorphineUpdate = true;
        }
        if (nbt.contains("hasTutorial"))
            hasTutorial = nbt.getBoolean("hasTutorial");
    }

    @Override
    public void tick(Level world, Player player) {
        if (isDead(player))
            return;
        world.getProfiler().push("FirstAidPlayerModel");
        if (sleepBlockTicks > 0)
            sleepBlockTicks--;
        else if (sleepBlockTicks < 0)
            throw new RuntimeException("Negative sleepBlockTicks " + sleepBlockTicks);

        float newCurrentHealth = calculateNewCurrentHealth(player);
        if (Float.isNaN(newCurrentHealth)) {
            FirstAid.LOGGER.warn("New current health is not a number, setting it to 0!");
            newCurrentHealth = 0F;
        }
        if (newCurrentHealth <= 0F) {
            FirstAid.LOGGER.error("Got {} health left, but isn't marked as dead!", newCurrentHealth);
            world.getProfiler().pop();
            return;
        }
        if (!world.isClientSide && resyncTimer != -1) {
            resyncTimer--;
            if (resyncTimer == 0) {
                resyncTimer = -1;
                FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new MessageSyncDamageModel(this, true));
            }
        }

        if (Float.isInfinite(newCurrentHealth)) {
            FirstAid.LOGGER.error("Error calculating current health: Value was infinite"); //Shouldn't happen anymore, but let's be safe
        } else {
            if (newCurrentHealth != prevHealthCurrent)
                ((SynchedEntityDataWrapper) player.entityData).set_impl(Player.DATA_HEALTH_ID, newCurrentHealth);
            prevHealthCurrent = newCurrentHealth;
        }

        if (!this.hasTutorial)
            this.hasTutorial = CapProvider.tutorialDone.contains(player.getName().getString());

        runScaleLogic(player);

        MobEffect morphineEffect = RegistryObjects.MORPHINE_EFFECT.get();
        //morphine update
        if (this.needsMorphineUpdate) {
            player.addEffect(new MobEffectInstance(morphineEffect, this.morphineTicksLeft, 0, false, false));
        }
        MobEffectInstance morphine = player.getEffect(morphineEffect);
        if (!this.needsMorphineUpdate) {
            this.morphineTicksLeft = morphine == null ? 0 : morphine.getDuration();
        }
        this.needsMorphineUpdate = false;

        //Debuff and part ticking
        world.getProfiler().push("PartDebuffs");
        forEach(part -> part.tick(world, player, morphine == null));
        if (morphine == null && !world.isClientSide)
            sharedDebuffs.forEach(sharedDebuff -> sharedDebuff.tick(player));
        world.getProfiler().pop();
        world.getProfiler().pop();
    }

    public static int getRandMorphineDuration() { //Tweak tooltip event when changing as well
        return ((EventHandler.RAND.nextInt(5) * 20 * 15) + 20 * 210);
    }

    @Deprecated
    @Override
    public void applyMorphine() {
        morphineTicksLeft = getRandMorphineDuration();
        needsMorphineUpdate = true;
    }

    @Override
    public void applyMorphine(Player player) {
        player.addEffect(new MobEffectInstance(RegistryObjects.MORPHINE_EFFECT.get(), getRandMorphineDuration(), 0, false, false));
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
            private byte count = 0;
            @Override
            public boolean hasNext() {
                return count < 8;
            }

            @Override
            public AbstractDamageablePart next() {
                if (count >= 8)
                    throw new NoSuchElementException();
                AbstractDamageablePart part = getFromEnum(EnumPlayerPart.VALUES[count]);
                count++;
                return part;
            }
        };
    }

    private float calculateNewCurrentHealth(Player player) {
        float currentHealth = 0;
        FirstAidConfig.Server.VanillaHealthCalculationMode mode = FirstAidConfig.SERVER.vanillaHealthCalculation.get();
        if (noCritical) mode = FirstAidConfig.Server.VanillaHealthCalculationMode.AVERAGE_ALL;
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
    public boolean isDead(@Nullable Player player) {
        boolean bleeding = PRCompatManager.getHandler().isBleeding(player);
        if (bleeding) {
            return true; //Technically not dead yet, but we should still return true to avoid running ticking and other logic
        }

        if (player != null && !player.isAlive())
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
    @OnlyIn(Dist.CLIENT)
    public int getMaxRenderSize() {
        int max = 0;
        for (AbstractDamageablePart part : this) {
            int newMax;
            if (FirstAidConfig.CLIENT.overlayMode.get() == FirstAidConfig.Client.OverlayMode.NUMBERS)
                newMax = Minecraft.getInstance().font.width(HealthRenderUtils.TEXT_FORMAT.format(part.currentHealth) + "/" + part.getMaxHealth()) + 1;
            else
                newMax = (int) (((((int) (part.getMaxHealth() + part.getAbsorption() + 0.9999F)) + 1) / 2F) * 9F);
            max = Math.max(max, newMax);
        }
        return max;
    }

    @Override
    public void sleepHeal(Player player) {
        if (sleepBlockTicks > 0)
            return;
        CommonUtils.healPlayerByPercentage(FirstAidConfig.SERVER.sleepHealPercentage.get(), this, player);
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
    public void revivePlayer(Player player) {
        if (FirstAidConfig.GENERAL.debug.get()) {
            CommonUtils.debugLogStacktrace("Reviving player");
        }
        player.revive();
        for (AbstractDamageablePart part : this) {
            if ((part.canCauseDeath || this.noCritical) && part.currentHealth <= 0F) {
                part.currentHealth = 1F; // Set the critical health to a non-zero value
            }
        }
        //make sure to resync the client health
        if (!player.level.isClientSide && player instanceof ServerPlayer)
            FirstAid.NETWORKING.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new MessageSyncDamageModel(this, true)); //Upload changes to the client
    }

    @Override
    public void runScaleLogic(Player player) {
        if (FirstAidConfig.SERVER.scaleMaxHealth.get()) { //Attempt to calculate the max health of the body parts based on the maxHealth attribute
            player.level.getProfiler().push("healthscaling");
            float globalFactor = player.getMaxHealth() / 20F;
            if (prevScaleFactor != globalFactor) {
                if (FirstAidConfig.GENERAL.debug.get()) {
                    FirstAid.LOGGER.info("Starting health scaling factor {} -> {} (max health {})", prevScaleFactor, globalFactor, player.getMaxHealth());
                }
                player.level.getProfiler().push("distribution");
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
                    if (FirstAidConfig.GENERAL.debug.get()) {
                        FirstAid.LOGGER.info("Part {} max health: {} initial; {} old; {} new", part.part.name(), part.initialMaxHealth, part.getMaxHealth(), result);
                    }
                    part.setMaxHealth(result);
                }
                player.level.getProfiler().popPush("correcting");
                if (Math.abs(expectedNewMaxHealth - newMaxHealth) >= 2F) {
                    if (FirstAidConfig.GENERAL.debug.get()) {
                        FirstAid.LOGGER.info("Entering second stage - diff {}", Math.abs(expectedNewMaxHealth - newMaxHealth));
                    }
                    List<AbstractDamageablePart> prioList = new ArrayList<>();
                    for (AbstractDamageablePart part : this) {
                        prioList.add(part);
                    }
                    prioList.sort(Comparator.comparingInt(AbstractDamageablePart::getMaxHealth));
                    for (AbstractDamageablePart part : prioList) {
                        int maxHealth = part.getMaxHealth();
                        if (FirstAidConfig.GENERAL.debug.get()) {
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
                player.level.getProfiler().pop();
            }
            prevScaleFactor = globalFactor;
            player.level.getProfiler().pop();
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
