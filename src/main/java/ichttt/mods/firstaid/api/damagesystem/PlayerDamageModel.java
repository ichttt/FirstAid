package ichttt.mods.firstaid.api.damagesystem;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface PlayerDamageModel extends EntityDamageModel {

    @SideOnly(Side.CLIENT)
    int getMaxRenderSize();

    void sleepHeal(EntityPlayer player);

    /**
     * Internal for PlayerRevive compat
     */
    void stopWaitingForHelp(EntityPlayer player);

    /**
     * Internal for PlayerRevive compat
     */
    boolean isWaitingForHelp();

    void revivePlayer(EntityPlayer entity);

    void scheduleResync();

    boolean hasTutorial();

    void setTutorial();

    DamageablePart getFromEnum(EnumPlayerPart part);
}
