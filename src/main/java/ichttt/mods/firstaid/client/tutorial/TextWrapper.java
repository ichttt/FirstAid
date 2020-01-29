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

package ichttt.mods.firstaid.client.tutorial;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class TextWrapper {
    private static final int maxChars = 35;
    private static final int minimumChars = 29;

    private int currentLine = 0;
    private final List<String> lines = new ArrayList<>();

    public TextWrapper(String text) {
        char[] chars = text.toCharArray();
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (char c : chars) {
            if (count < minimumChars) {
                builder.append(c);
            } else if (c == ' ') {
                lines.add(builder.toString());
                builder = new StringBuilder();
                count = 0;
            } else if (count >= maxChars) {
                builder.append('-');
                lines.add(builder.toString());
                builder = new StringBuilder();
                builder.append(c);
                count = 0;
            } else {
                builder.append(c);
            }
            count++;
        }
        String last = builder.toString();
        if (!last.equals("")) {
            lines.add(last);
        }
    }

    public int getRemainingLines() {
        return lines.size() - currentLine;
    }

    public String nextLine() {
        String s = lines.get(currentLine);
        currentLine++;
        return s;
    }
}
