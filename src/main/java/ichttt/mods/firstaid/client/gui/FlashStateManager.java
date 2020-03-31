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

package ichttt.mods.firstaid.client.gui;

public class FlashStateManager {
    private long startTime;
    private int currentState = 0;

    public void setActive(long startTime) {
        this.startTime = startTime;
        currentState = 1;
    }

    public boolean update(long worldTime) {
        if (isPaused())
            return false;
        currentState = (int) ((worldTime - startTime) / 150) + 1;
        if (currentState >= 8)
            currentState = 0;
        if (isPaused())
            return false;
        return currentState % 2 == 0;
    }

    public boolean isPaused() {
        return currentState == 0;
    }
}
