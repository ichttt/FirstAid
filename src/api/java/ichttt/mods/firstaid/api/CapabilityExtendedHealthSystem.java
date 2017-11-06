package ichttt.mods.firstaid.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class CapabilityExtendedHealthSystem {

    @SuppressWarnings("unused")
    @CapabilityInject(AbstractPlayerDamageModel.class)
    public static Capability<AbstractPlayerDamageModel> INSTANCE;

    public static void register() {
        CapabilityManager.INSTANCE.register(AbstractPlayerDamageModel.class, new Capability.IStorage<AbstractPlayerDamageModel>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<AbstractPlayerDamageModel> capability, AbstractPlayerDamageModel instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<AbstractPlayerDamageModel> capability, AbstractPlayerDamageModel instance, EnumFacing side, NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
        }
        , DefaultImpl::new);
    }

    private static class DefaultImpl extends AbstractPlayerDamageModel {

        public DefaultImpl() {
            super(null, null, null, null, null, null, null, null);
        }

        @Override
        public void tick(World world, EntityPlayer player) {}

        @Override
        public void applyMorphine() {}

        @Override
        public int getMorphineTicks() {
            return 0;
        }

        @Override
        public float getCurrentHealth() {
            return 0;
        }

        @Override
        public boolean isDead() {
            return false;
        }

        @Override
        public Float getAbsorption() {
            return null;
        }

        @Override
        public void setAbsorption(float absorption) {}

        @Override
        public int getMaxRenderSize() {
            return 0;
        }

        @SuppressWarnings("ConstantConditions")
        @Nonnull
        @Override
        public Iterator<AbstractDamageablePart> iterator() {
            return null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return null;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) { }
    }
}
