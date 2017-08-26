package de.technikforlife.firstaid.damagesystem;

import de.technikforlife.firstaid.damagesystem.enums.EnumWoundState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class DamageablePart {

    public final float maxHealth;
    public final boolean canCauseDeath;
    @Nullable
    private PartHealer activeHealer;
    private UUID playerUUID;

    @Nonnull
    private EnumWoundState state = EnumWoundState.HEALTHY;
    public float currentHealth;

    public DamageablePart(float maxHealth, boolean canCauseDeath) {
        this.maxHealth = maxHealth;
        this.canCauseDeath = canCauseDeath;
        currentHealth = maxHealth;
    }

    public EnumWoundState getWoundState() {
        return state;
    }

    /**
     * @return true if the {@link EnumWoundState} has changed
     */
    public boolean heal(float amount, EntityLivingBase toHeal) {
        EnumWoundState prev = state;
        currentHealth = Math.min(maxHealth, amount + currentHealth);
        state = EnumWoundState.getWoundState(maxHealth, currentHealth);
        toHeal.heal(amount);
        return prev != state;
    }

    /**
     * @return true if the player drops below/ has 0 HP
     */
    public boolean damage(float amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        return currentHealth == 0;
    }

    void tick(World world) {
        if (activeHealer != null) {
            if (activeHealer.tick()) {
                EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(playerUUID);
                Objects.requireNonNull(player);
                heal(1F, player);
                world.playEvent(2005, player.getPosition(), 0);
            }
            if (activeHealer.hasFinished())
                activeHealer = null;
        }
    }

    public void applyItem(PartHealer healer, UUID playerUUID) {
        activeHealer = healer;
        this.playerUUID = playerUUID;
    }
}
