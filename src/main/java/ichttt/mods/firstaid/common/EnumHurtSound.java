package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.client.DebuffTimedSound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum EnumHurtSound {
    NONE {
        @SideOnly(Side.CLIENT)
        @Override
        public void playSound(int duration) {}
    },
    HEARTBEAT {
        @SideOnly(Side.CLIENT)
        @Override
        public void playSound(int duration) {
            DebuffTimedSound.playHurtSound(duration);
        }
    };

    public abstract void playSound(int duration);

}
