/*
 * FirstAid API
 * Copyright (c) 2017-2020
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

/**
 * API for the FirstAid mod:
 * List of API versions mapped to FirstAid version where they have been introduced:
 * API Version: FirstAid Version (Major Changes)
 * 1:  1.3.2 (Initial draft)
 * 2:  1.3.3 (Introduction of the registry)
 * 3:  1.4.0 (Minor changes)
 * 4:  1.4.3 (Overhaul of the registry, especially healing item and debuff registry)
 * 5:  1.4.4 (Minor changes)
 * 6:  1.4.6 (Minor changes)
 * 7:  1.5.0 (Apply time for healing items added)
 * 8:  1.5.6 (Removed deprecated methods and fields)
 * 9:  1.5.7 (Minor changes)
 * 10: 1.5.9 (ItemHealing addition)
 * 11: 1.5.10 (BREAKING CHANGE - Changed debuffs registration to supplier based)
 * 12: 1.6.5 (Minor changes)
 * 13: 1.6.7 (Debuffs now take DamageSource instead of String, FirstAidLivingDamageEvent)
 * 14: 1.6.13 (Restructure of DamageDistribution registering/building)
 */
@API(apiVersion = "14", provides = "FirstAid API", owner = "firstaid")
package ichttt.mods.firstaid.api;

import net.minecraftforge.fml.common.API;