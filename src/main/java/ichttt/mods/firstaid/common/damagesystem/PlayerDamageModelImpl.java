package ichttt.mods.firstaid.common.damagesystem;

import com.creativemd.playerrevive.api.IRevival;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.CapProvider;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class PlayerDamageModelImpl extends EntityDamageModelImpl implements PlayerDamageModel {
    private boolean waitingForHelp = false;
    private int sleepBlockTicks = 0;
    private int resyncTimer = -1;
    private boolean hasTutorial;

    protected PlayerDamageModelImpl(IDebuff[] headDebuffs, IDebuff[] bodyDebuffs, IDebuff[] armDebuffs, IDebuff[] legFootDebuffs) {
        super(headDebuffs, bodyDebuffs, armDebuffs, legFootDebuffs);
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
        for (DamageablePart part : this) {
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
        for (DamageablePart part : this) {
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
}
