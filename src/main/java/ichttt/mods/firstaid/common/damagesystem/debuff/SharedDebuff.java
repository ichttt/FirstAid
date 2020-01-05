/*
 * FirstAid
 * Copyright (C) 2017-2019
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
import ichttt.mods.firstaid.api.damagesystem.DamageablePart;
import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import ichttt.mods.firstaid.api.debuff.IDebuff;
import ichttt.mods.firstaid.api.enums.EnumBodyPart;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;
import net.minecraft.entity.EntityLivingBase;

import java.util.Objects;

public class SharedDebuff implements IDebuff {
    private final IDebuff debuff;
    private final EnumBodyPart[] parts;
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
    public void handleDamageTaken(float damage, float healthPerMax, EntityLivingBase entity) {
        if (debuff.isEnabled()) {
            this.damage += damage;
            this.damageCount++;
        }
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityLivingBase entity) {
        if (debuff.isEnabled()) {
            this.healingDone += healingDone;
            this.healingCount++;
        }
    }

    public void tick(EntityLivingBase entity) {
        if (!debuff.isEnabled() || entity.world.isRemote)
            return;

        EntityDamageModel damageModel = Objects.requireNonNull(entity.getCapability(CapabilityExtendedHealthSystem.INSTANCE, null));
        float healthPerMax = 0;
        for (EnumBodyPart part : parts) {
            DamageablePart damageablePart = damageModel.getFromEnum(part);
            healthPerMax += damageablePart.getCurrentHealth() / damageablePart.getMaxHealth();
        }

        healthPerMax /= parts.length;
        if (healingCount > 0) {
            this.healingDone /= healingCount;
            debuff.handleHealing(this.healingDone, healthPerMax, entity);
        }
        if (damageCount > 0) {
            this.damage /= damageCount;
            debuff.handleDamageTaken(this.damage, healthPerMax, entity);
        }
        this.healingDone = 0;
        this.damage = 0;
        this.damageCount = 0;
        this.healingCount = 0;

        debuff.update(entity, healthPerMax);
    }
}
