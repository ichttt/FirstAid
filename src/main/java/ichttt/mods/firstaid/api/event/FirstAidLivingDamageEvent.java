/*
 * FirstAid API
 * Copyright (c) 2017-2019
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

import ichttt.mods.firstaid.api.damagesystem.EntityDamageModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Fired when the damage has been applied.
 * Canceling this event will cause the damage to be reset to {@link #getBeforeDamage()}
 */
@Cancelable
public class FirstAidLivingDamageEvent extends LivingEvent {
    private final EntityDamageModel afterDamageDone;
    private final EntityDamageModel beforeDamageDone;
    private final DamageSource source;
    private final float undistributedDamage;

    public FirstAidLivingDamageEvent(EntityLivingBase entity, EntityDamageModel afterDamageDone, EntityDamageModel beforeDamageDone, DamageSource source, float undistributedDamage) {
        super(entity);
        this.afterDamageDone = afterDamageDone;
        this.beforeDamageDone = beforeDamageDone;
        this.source = source;
        this.undistributedDamage = undistributedDamage;
    }

    /**
     * @return The damage model before this damage got applied. Canceling this event causes this to be restored
     */
    public EntityDamageModel getAfterDamage() {
        return this.afterDamageDone;
    }

    /**
     * @return The damage model after this damage got applied. Not canceling this event causes this to applied
     */
    public EntityDamageModel getBeforeDamage() {
        return this.beforeDamageDone;
    }

    /**
     * @return The source from where the damage came
     */
    public DamageSource getSource() {
        return this.source;
    }

    /**
     * @return The damage that could not be distributed on any
     */
    public float getUndistributedDamage() {
        return this.undistributedDamage;
    }
}
