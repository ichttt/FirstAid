/*
 * FirstAid
 * Copyright (C) 2017-2023
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


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TutorialAction {
    private final List<Object> queue = new ArrayList<>();
    private final GuiTutorial guiContext;
    private int pos = 0;
    private TextWrapper activeWrapper;
    private String s1, s2;

    public TutorialAction(GuiTutorial guiContext) {
        this.guiContext = guiContext;
    }

    public void draw(GuiGraphics stack) {
        if (s2 != null) {
            guiContext.drawOffsetString(stack, s1, 4);
            guiContext.drawOffsetString(stack, s2, 16);
        } else if (s1 != null) {
            guiContext.drawOffsetString(stack, s1, 10);
        }
    }

    @SuppressWarnings("unchecked")
    public void next() {
        if (activeWrapper != null) {
            writeFromActiveWrapper();
            return;
        }
        Object obj = queue.get(pos);
        if (obj instanceof TextWrapper) {
            activeWrapper = (TextWrapper) obj;
            writeFromActiveWrapper();
            pos++;
        } else if (obj instanceof Consumer) {
            Consumer<GuiTutorial> consumer = (Consumer<GuiTutorial>) obj;
            consumer.accept(guiContext);
            pos++;
            if (hasNext()) next();
        } else {
            throw new RuntimeException("Found invalid object " + obj.toString());
        }
    }

    public boolean hasNext() {
        return pos < queue.size() || activeWrapper != null;
    }

    private void writeFromActiveWrapper() {
        s1 = activeWrapper.nextLine();
        if (activeWrapper.getRemainingLines() >= 1) {
            s2 = activeWrapper.nextLine();
        } else
            s2 = null;
        if (activeWrapper.getRemainingLines() < 1)
            activeWrapper = null;
    }

    public void addTextWrapper(String i18nKey, String... format) {
        //noinspection RedundantCast - cast is needed to avoid ambigious call
        queue.add(new TextWrapper(I18n.get(i18nKey, (Object[]) format)));
    }

    public void addActionCallable(Consumer<GuiTutorial> callable) {
        queue.add(callable);
    }
}
