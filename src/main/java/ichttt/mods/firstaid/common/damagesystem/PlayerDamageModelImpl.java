package ichttt.mods.firstaid.common.damagesystem;

import com.creativemd.playerrevive.api.IRevival;
import com.google.common.collect.ImmutableList;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.FirstAidRegistry;
import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import ichttt.mods.firstaid.api.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumPlayerDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerDamageModelImpl extends EntityDamageModelImpl implements PlayerDamageModel {
    protected final DamageablePart head;
    protected final DamageablePart leftArm;
    protected final DamageablePart leftLeg;
    protected final DamageablePart leftFoot;
    protected final DamageablePart body;
    protected final DamageablePart rightArm;
    protected final DamageablePart rightLeg;
    protected final DamageablePart rightFoot;
    private boolean waitingForHelp = false;
    private int sleepBlockTicks = 0;
    private int resyncTimer = -1;
    private boolean hasTutorial;


    public static PlayerDamageModel createPlayer() {
        FirstAidRegistry registry = FirstAidRegistryImpl.INSTANCE;
        Set<SharedDebuff> sharedDebuffs = new HashSet<>();
        IDebuff[] headDebuffs = registry.getPlayerDebuff(EnumPlayerDebuffSlot.HEAD);
        IDebuff[] bodyDebuffs = registry.getPlayerDebuff(EnumPlayerDebuffSlot.BODY);
        IDebuff[] armsDebuffs = registry.getPlayerDebuff(EnumPlayerDebuffSlot.ARMS);
        IDebuff[] legFootDebuffs = registry.getPlayerDebuff(EnumPlayerDebuffSlot.LEGS_AND_FEET);
        for (IDebuff debuff : armsDebuffs)
            sharedDebuffs.add((SharedDebuff) debuff);
        for (IDebuff debuff : legFootDebuffs)
            sharedDebuffs.add((SharedDebuff) debuff);
        DamageablePart head = new DamageablePartImpl("head", FirstAidConfig.damageSystem.maxHealthHead, FirstAidConfig.damageSystem.causeDeathHead, headDebuffs);
        DamageablePart leftArm = new DamageablePartImpl("leftArm", FirstAidConfig.damageSystem.maxHealthLeftArm, false, armsDebuffs);
        DamageablePart leftLeg = new DamageablePartImpl("leftLeg", FirstAidConfig.damageSystem.maxHealthLeftLeg, false, legFootDebuffs);
        DamageablePart leftFoot = new DamageablePartImpl("leftFoot", FirstAidConfig.damageSystem.maxHealthLeftFoot, false, legFootDebuffs);
        DamageablePart body = new DamageablePartImpl("body", FirstAidConfig.damageSystem.maxHealthBody, FirstAidConfig.damageSystem.causeDeathBody, bodyDebuffs);
        DamageablePart rightArm = new DamageablePartImpl("rightArm", FirstAidConfig.damageSystem.maxHealthRightArm, false, armsDebuffs);
        DamageablePart rightLeg = new DamageablePartImpl("rightLeg", FirstAidConfig.damageSystem.maxHealthRightLeg, false, legFootDebuffs);
        DamageablePart rightFoot = new DamageablePartImpl("rightFoot", FirstAidConfig.damageSystem.maxHealthRightFoot, false, legFootDebuffs);
        Map<EntityEquipmentSlot, ImmutableList<DamageablePart>> map = new HashMap<>();
        map.put(EntityEquipmentSlot.HEAD, new ImmutableList.Builder<DamageablePart>().add(head).build());
        map.put(EntityEquipmentSlot.CHEST, new ImmutableList.Builder<DamageablePart>().add(leftArm, body, rightArm).build());
        map.put(EntityEquipmentSlot.LEGS, new ImmutableList.Builder<DamageablePart>().add(leftLeg, rightLeg).build());
        map.put(EntityEquipmentSlot.FEET, new ImmutableList.Builder<DamageablePart>().add(leftFoot, rightFoot).build());

        return new PlayerDamageModelImpl(sharedDebuffs, map, head, leftArm, leftLeg, leftFoot, body, rightArm, rightLeg, rightFoot);
    }

    protected PlayerDamageModelImpl(Set<SharedDebuff> sharedDebuffs, Map<EntityEquipmentSlot, ImmutableList<DamageablePart>> map, DamageablePart head, DamageablePart leftArm, DamageablePart leftLeg, DamageablePart leftFoot, DamageablePart body, DamageablePart rightArm, DamageablePart rightLeg, DamageablePart rightFoot) {
        super(null, map, !FirstAidConfig.damageSystem.causeDeathBody && !FirstAidConfig.damageSystem.causeDeathHead, sharedDebuffs);
        this.head = head;
        this.leftArm = leftArm;
        this.leftLeg = leftLeg;
        this.leftFoot = leftFoot;
        this.body = body;
        this.rightArm = rightArm;
        this.rightLeg = rightLeg;
        this.rightFoot = rightFoot;
    }

    @Override
    public boolean tick(World world, EntityLivingBase entity) {
        boolean result = super.tick(world, entity);
        if (result) {
            if (sleepBlockTicks > 0)
                sleepBlockTicks--;
            else if (sleepBlockTicks < 0)
                throw new RuntimeException("Negative sleepBlockTicks " + sleepBlockTicks);
            if (!world.isRemote && resyncTimer != -1) {
                resyncTimer--;
                if (resyncTimer == 0) {
                    resyncTimer = -1;
                    FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(this), (EntityPlayerMP) entity);
                }
            }

            if (!this.hasTutorial)
                this.hasTutorial = CapProvider.tutorialDone.contains(entity.getName());
        }
        return result;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = super.serializeNBT();
        tagCompound.setBoolean("hasTutorial", hasTutorial);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        if (nbt.hasKey("hasTutorial"))
            hasTutorial = nbt.getBoolean("hasTutorial");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getMaxRenderSize() {
        int max = 0;
        for (DamageablePart part : this.getParts()) {
            int newMax;
            if (FirstAidConfig.overlay.overlayMode == FirstAidConfig.Overlay.OverlayMode.NUMBERS)
                newMax = Minecraft.getMinecraft().fontRenderer.getStringWidth(HealthRenderUtils.TEXT_FORMAT.format(part.getCurrentHealth()) + "/" + part.getMaxHealth()) + 1;
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

    @Override
    public void revivePlayer(EntityPlayer entity) {
        if (FirstAidConfig.debug) {
            CommonUtils.debugLogStacktrace("Reviving player");
        }
        entity.isDead = false;
        for (DamageablePart part : this.getParts()) {
            if ((part.canCauseDeath || this.noCritical) && part.getCurrentHealth() <= 0F) {
                part.setCurrentHealth(1F); // Set the critical health to a non-zero value
            }
        }
        //make sure to resync the client health
        if (!entity.world.isRemote && entity instanceof EntityPlayerMP)
            FirstAid.NETWORKING.sendTo(new MessageSyncDamageModel(this), (EntityPlayerMP) entity); //Upload changes to the client
    }

    @Override
    public boolean isDead(@Nullable EntityLivingBase entity) {
        EntityPlayer player = (EntityPlayer) entity;
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
        return super.isDead(entity);
    }

    @Override
    public void scheduleResync() {
        if (this.resyncTimer == -1) {
            this.resyncTimer = 2;
        } else {
            FirstAid.LOGGER.warn("resync already scheduled!");
        }
    }

    @Override
    public boolean hasTutorial() {
        return this.hasTutorial;
    }

    @Override
    public void setTutorial() {
        this.hasTutorial = true;
    }


    @Override
    public DamageablePart getFromEnum(EnumPlayerPart part) {
        switch (part) {
            case HEAD:
                return head;
            case LEFT_ARM:
                return leftArm;
            case LEFT_LEG:
                return leftLeg;
            case BODY:
                return body;
            case RIGHT_ARM:
                return rightArm;
            case RIGHT_LEG:
                return rightLeg;
            case LEFT_FOOT:
                return leftFoot;
            case RIGHT_FOOT:
                return rightFoot;
            default:
                throw new RuntimeException("Unknown enum " + part);
        }
    }

    @Override
    public EntityDamageModel createCopy() {
        return createPlayer();
    }

    @Override
    public EntityEquipmentSlot getHitSlot(float yRelative) {
        if (yRelative > 1.5)
            return EntityEquipmentSlot.HEAD;
        else if (yRelative > 0.8)
            return EntityEquipmentSlot.CHEST;
        else if (yRelative > 0.4)
            return EntityEquipmentSlot.LEGS;
        else
            return EntityEquipmentSlot.FEET;
    }
}
