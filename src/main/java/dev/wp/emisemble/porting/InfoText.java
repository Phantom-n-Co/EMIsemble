package dev.wp.emisemble.porting;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Info-page text: EMI splits each component for wrapping and eats embedded breaks, so a line is a component. */
public final class InfoText {
    // Hosts ship breaks as real newlines and, where the lang value survived an extra escape, as a literal backslash-n.
    private static final Pattern BREAK = Pattern.compile("\\\\+n|\\r?\\n");

    private InfoText() {}

    /** One component per line, keeping the component untouched when it has no break to split on. */
    public static List<Component> lines(Component text) {
        String flat = text.getString();
        if (!BREAK.matcher(flat).find()) {
            return List.of(text);
        }
        List<Component> lines = new ArrayList<>();
        for (String line : BREAK.split(flat)) {
            // A blank line has to carry a glyph, or the paragraph break collapses into the line above it.
            lines.add(Component.literal(line.isBlank() ? " " : line).withStyle(text.getStyle()));
        }
        return lines;
    }
}
