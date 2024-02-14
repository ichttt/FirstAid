/*
 * FirstAid API
 * Copyright (c) 2017-2024
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

package ichttt.mods.firstaid.api.debuff;

import com.mojang.serialization.Codec;
import ichttt.mods.firstaid.api.enums.EnumDebuffSlot;

public interface IDebuffBuilder {

    /**
     * @return A codec for serialization
     */
    Codec<? extends IDebuffBuilder> codec();

    EnumDebuffSlot affectedSlot();

    /**
     * Builds a new debuff instance based on the builder.
     * Each call to this should create a new instance
     * @return A new debuff instance
     */
    IDebuff build();
}
