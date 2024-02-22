/*
 * FirstAid
 * Copyright (C) 2017-2024
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

package ichttt.mods.firstaid.common.util;


import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class LoggingMarkers {
    public static final Marker DAMAGE_DISTRIBUTION = MarkerManager.getMarker("damage_distribution");
    public static final Marker NETWORK = MarkerManager.getMarker("network");
    public static final Marker REGISTRY = MarkerManager.getMarker("registry");
}
