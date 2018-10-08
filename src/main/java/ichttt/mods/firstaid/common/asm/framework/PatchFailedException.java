/*
 * FirstAid
 * Copyright (C) 2017-2018
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

package ichttt.mods.firstaid.common.asm.framework;

public class PatchFailedException extends RuntimeException {

    private static String format(String className, String message) {
        return "There was a severe error while patching the class " + className + " and loading cannot continue.\n" +
                "The reason is most likely that the source file is different from what we expect.\n" +
                "This may be because of other coremods or an invalid minecraft installation.\n" +
                "Further details: " + message;
    }

    public PatchFailedException(String className, String message) {
        super(format(className, message));
    }

    public PatchFailedException(String className, String message, Throwable cause) {
        super(format(className, message), cause);
    }
}
