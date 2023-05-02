/*
 * FirstAid API
 * Copyright (c) 2017-2023
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

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
