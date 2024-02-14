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

package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.healing.ItemHealing;
import ichttt.mods.firstaid.common.damagesystem.PartHealer;
import ichttt.mods.firstaid.common.items.ItemMorphine;
import ichttt.mods.firstaid.common.potion.FirstAidPotion;
import ichttt.mods.firstaid.common.potion.PotionPoisonPatched;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegistryObjects {
    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, FirstAid.MODID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENT_REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FirstAid.MODID);
    private static final DeferredRegister<MobEffect> MOB_EFFECT_REGISTER = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, FirstAid.MODID);
    private static final DeferredRegister<MobEffect> MOB_EFFECT_OVERRIDE_REGISTER = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "minecraft");
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FirstAid.MODID);

    public static final RegistryObject<Item> BANDAGE;
    public static final RegistryObject<Item> PLASTER;
    public static final RegistryObject<Item> MORPHINE;

    public static final RegistryObject<SoundEvent> HEARTBEAT;

    public static final RegistryObject<MobEffect> MORPHINE_EFFECT;
    public static final RegistryObject<MobEffect> POISON_PATCHED;

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB;

    static {
        FirstAidConfig.Server server = FirstAidConfig.SERVER;

        // ITEMS
        BANDAGE = ITEM_REGISTER.register("bandage", () -> ItemHealing.create(new Item.Properties().stacksTo(16), stack -> new PartHealer(() -> server.bandage.secondsPerHeal.get() * 20, server.bandage.totalHeals::get, stack), stack -> server.bandage.applyTime.get()));
        PLASTER = ITEM_REGISTER.register("plaster", () -> ItemHealing.create(new Item.Properties().stacksTo(16), stack -> new PartHealer(() -> server.plaster.secondsPerHeal.get() * 20, server.plaster.totalHeals::get, stack), stack -> server.plaster.applyTime.get()));
        MORPHINE = ITEM_REGISTER.register("morphine", ItemMorphine::new);

        // SOUNDS
        ResourceLocation soundLocation = new ResourceLocation(FirstAid.MODID, "debuff.heartbeat");
        HEARTBEAT = SOUND_EVENT_REGISTER.register(soundLocation.getPath(), () -> SoundEvent.createVariableRangeEvent(soundLocation));

        // MOB EFFECTS
        MORPHINE_EFFECT = MOB_EFFECT_REGISTER.register("morphine", () -> new FirstAidPotion(MobEffectCategory.BENEFICIAL, 0xDDD));
        POISON_PATCHED = MOB_EFFECT_OVERRIDE_REGISTER.register("poison", () -> new PotionPoisonPatched(MobEffectCategory.HARMFUL, 5149489));

        // CREATIVE MODE TABS
        CREATIVE_TAB = CREATIVE_MODE_TAB_REGISTER.register("main_tab", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.firstaid"))
                .icon(() -> new ItemStack(BANDAGE.get()))
                .build());
    }


    public static void registerToBus(IEventBus bus) {
        ITEM_REGISTER.register(bus);
        SOUND_EVENT_REGISTER.register(bus);
        MOB_EFFECT_REGISTER.register(bus);
        MOB_EFFECT_OVERRIDE_REGISTER.register(bus);
        CREATIVE_MODE_TAB_REGISTER.register(bus);
    }
}
