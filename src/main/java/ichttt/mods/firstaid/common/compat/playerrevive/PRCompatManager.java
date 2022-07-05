/*
 * FirstAid
 * Copyright (C) 2017-2022
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

package ichttt.mods.firstaid.common.compat.playerrevive;

import com.google.common.base.Preconditions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public class PRCompatManager {
    private static IPRCompatHandler handler = new NoopPRCompatHandler();

    public static IPRCompatHandler getHandler() {
        return handler;
    }

    public static void init() {
        if (ModList.get().isLoaded("playerrevive")) {
            loadCompatClass();
        }
    }

    private static void loadCompatClass() {
        if (PRPresentCompatHandler.canUse()) {
            handler = new PRPresentCompatHandler();
            MinecraftForge.EVENT_BUS.register(PlayerReviveEventHandler.class);
        }
    }
}
