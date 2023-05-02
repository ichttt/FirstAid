package ichttt.mods.firstaid.api.event;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RegisterHealingTypeEvent extends FirstAidRegisterEvent {
    private final Map<Item, Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>>> healerMap = new HashMap<>();

    public RegisterHealingTypeEvent(Level level) {
        super(level);
    }

    public void registerHealingType(@Nonnull Item item, @Nonnull Function<ItemStack, AbstractPartHealer> factory, Function<ItemStack, Integer> applyTime) {
        if (this.healerMap.containsKey(item))
            FirstAid.LOGGER.warn("Healing type override detected for item " + item);
        this.healerMap.put(item, Pair.of(factory, applyTime));
    }

    public Map<Item, Pair<Function<ItemStack, AbstractPartHealer>, Function<ItemStack, Integer>>> getHealerMap() {
        return Collections.unmodifiableMap(healerMap);
    }
}
