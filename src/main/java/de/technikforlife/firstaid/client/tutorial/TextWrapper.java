package de.technikforlife.firstaid.client.tutorial;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class TextWrapper {
    private static final int maxChars = 34;
    private static final int minimumChars = 28;

    private int currentLine = 0;
    private List<String> lines = new ArrayList<>();

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
