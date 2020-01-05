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

package ichttt.mods.firstaid.api.damagesystem;

import ichttt.mods.firstaid.api.enums.EnumBodyPart;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public interface EntityDamageModel extends Iterable<DamageablePart>, INBTSerializable<NBTTagCompound> {
    DamageablePart getFromEnum(EnumBodyPart part);

    /**
     * Updates the part.
     * Should not be called by other mods!
     */
    boolean tick(World world, EntityLivingBase entity);

    /**
     * @deprecated Migrated to a potion effect, pass it in to directly apply
     */
    @Deprecated
    void applyMorphine();

    void applyMorphine(EntityLivingBase entity);

    int getMorphineTicks();

    float getCurrentHealth();

    /**
     * Checks if the player is dead.
     * This does not mean that the player cannot be revived.
     *
     * @param entity The player to check. If null, it will not be checked if the player can be revived (Using PlayerRevival)
     * @return true if dead, false otherwise
     */
    boolean isDead(@Nullable EntityLivingBase entity);

    Float getAbsorption();

    void setAbsorption(float absorption);

    int getCurrentMaxHealth();

    void runScaleLogic(EntityLivingBase entity);

    boolean hasNoCritical();
}
