package ichttt.mods.firstaid.common.apiimpl;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.registry.DamageSourceEntry;
import ichttt.mods.firstaid.api.registry.DebuffEntry;
import ichttt.mods.firstaid.api.registry.HealingEntry;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;
import java.util.Objects;

public class FirstAidRegistries {
    public static IForgeRegistry<HealingEntry> HEALING;
    public static IForgeRegistry<DamageSourceEntry> DAMAGE_SOURCE;
    public static IForgeRegistry<DebuffEntry> DEBUFF;

    public static void createRegistries() {
        HEALING = new RegistryBuilder<HealingEntry>()
                .setType(HealingEntry.class)
                .setMaxID(Byte.MAX_VALUE)
                .setName(new ResourceLocation(FirstAid.MODID, "healing"))
                .add((IForgeRegistry.ValidateCallback<HealingEntry>) (owner, stage, id, key, obj) -> {
                    for (HealingEntry type : HEALING.getValuesCollection()) {
                        if (type.item == obj.item)
                            throw new IllegalStateException("Got an entry for the same item: " + obj.item + " from " + obj.getRegistryName() + " and " + type.getRegistryName());
                    }
                })
                .create();
        DAMAGE_SOURCE = new RegistryBuilder<DamageSourceEntry>()
                .setType(DamageSourceEntry.class)
                .setName(new ResourceLocation(FirstAid.MODID, "damage_source"))
                .disableSaving()
                .create();
        DEBUFF = new RegistryBuilder<DebuffEntry>()
                .setType(DebuffEntry.class)
                .setMaxID(Short.MAX_VALUE)
                .setName(new ResourceLocation(FirstAid.MODID, "debuff"))
                .create();
    }

    @Nonnull
    public static HealingEntry getHealingEntry(Item item) {
        return Objects.requireNonNull(HEALING.getValue(item.getRegistryName()), "Did not find item " + item.getRegistryName());
    }
}
