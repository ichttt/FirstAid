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
 */
@API(apiVersion = "10", provides = "FirstAid API", owner = "firstaid")
package ichttt.mods.firstaid.api;

import net.minecraftforge.fml.common.API;