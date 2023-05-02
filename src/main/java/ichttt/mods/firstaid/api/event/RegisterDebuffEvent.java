package ichttt.mods.firstaid.api.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import ichttt.mods.firstaid.api.debuff.DebuffBuilderFactory;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.common.damagesystem.debuff.SharedDebuff;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class RegisterDebuffEvent extends FirstAidRegisterEvent {

    private final DebuffBuilderFactory debuffBuilderFactory;
    private final Multimap<EnumDebuffSlot, Supplier<IDebuff>> debuffs = HashMultimap.create();


    public RegisterDebuffEvent(Level level, DebuffBuilderFactory debuffBuilderFactory) {
        super(level);
        this.debuffBuilderFactory = debuffBuilderFactory;
    }

    public DebuffBuilderFactory getDebuffBuilderFactory() {
        return debuffBuilderFactory;
    }

    public void registerDebuff(Supplier<IDebuff> debuffFactory, EnumDebuffSlot slot) {
        if (slot.playerParts.length > 1 && !(debuffFactory instanceof SharedDebuff)) {
            this.debuffs.put(slot, () -> new SharedDebuff(debuffFactory.get(), slot));
        } else {
            debuffs.put(slot, debuffFactory);
        }
    }

    public Multimap<EnumDebuffSlot, Supplier<IDebuff>> getDebuffs() {
        return ImmutableMultimap.copyOf(debuffs);
    }
}
