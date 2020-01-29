/*
 * FirstAid
 * Copyright (C) 2017-2020
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

package ichttt.mods.firstaid.common.damagesystem.debuff;

import ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Objects;

public class SharedDebuff implements IDebuff {
    private final IDebuff debuff;
    private final EnumPlayerPart[] parts;
    private int damage;
    private int healingDone;
    private int damageCount;
    private int healingCount;

    public SharedDebuff(IDebuff debuff, EnumDebuffSlot slot) {
        if (slot.playerParts.length <= 1)
            throw new IllegalArgumentException("Only slots with more then more parts can be wrapped by SharedDebuff!");
        this.debuff = debuff;
        this.parts = slot.playerParts;
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayerMP player) {
        if (debuff.isEnabled()) {
            this.damage += damage;
            this.damageCount++;
        }
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayerMP player) {
        if (debuff.isEnabled()) {
            this.healingDone += healingDone;
            this.healingCount++;
        }
    }

    public void tick(EntityPlayer player) {
        if (!debuff.isEnabled() || player.world.isRemote || !(player instanceof EntityPlayerMP))
            return;

        AbstractPlayerDamageModel damageModel = Objects.requireNonNull(player.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
        float healthPerMax = 0;
        for (EnumPlayerPart part : parts) {
            AbstractDamageablePart damageablePart = damageModel.getFromEnum(part);
            healthPerMax += damageablePart.currentHealth / damageablePart.getMaxHealth();
        }

        healthPerMax /= parts.length;
        if (healingCount > 0) {
            this.healingDone /= healingCount;
            debuff.handleHealing(this.healingDone, healthPerMax, (EntityPlayerMP) player);
        }
        if (damageCount > 0) {
            this.damage /= damageCount;
            debuff.handleDamageTaken(this.damage, healthPerMax, (EntityPlayerMP) player);
        }
        this.healingDone = 0;
        this.damage = 0;
        this.damageCount = 0;
        this.healingCount = 0;

        debuff.update(player, healthPerMax);
    }
}
