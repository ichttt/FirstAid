package de.technikforlife.firstaid.client.tutorial;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class TutorialAction {
    private final List<Object> queue = new ArrayList<>();
    private final GuiTutorial guiContext;
    private int pos = 0;
    private TextWrapper activeWrapper;
    private String s1, s2;

    public TutorialAction(GuiTutorial guiContext) {
        this.guiContext = guiContext;
    }

    public void draw() {
        if (s2 != null) {
            guiContext.drawOffsetString(s1, 4);
            guiContext.drawOffsetString(s2, 16);
        } else if (s1 != null) {
            guiContext.drawOffsetString(s1, 10);
        }
    }

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
            //noinspection unchecked
            Consumer<GuiTutorial> consumer = (Consumer<GuiTutorial>) obj;
            consumer.accept(guiContext);
            pos++;
            next();
        }
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

    public void addTextWrapper(TextWrapper wrapper) {
        queue.add(wrapper);
    }

    public void addTextWrapper(String i18nKey) {
        queue.add(new TextWrapper(I18n.format(i18nKey)));
    }

    public void addActionCallable(Consumer<GuiTutorial> callable) {
        queue.add(callable);
    }
}
