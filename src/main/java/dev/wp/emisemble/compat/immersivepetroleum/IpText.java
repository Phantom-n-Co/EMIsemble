package dev.wp.emisemble.compat.immersivepetroleum;

import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/**
 * The energy/duration pair every IP page ends with, placed where the host puts it: centred, starting at height -
 * 18.
 */
public final class IpText {
    private static final int LINE_HEIGHT = 9;
    private static final int BLOCK_HEIGHT = LINE_HEIGHT * 2;
    private static final int COLOR = 0xFFFFFFFF;

    private IpText() {}

    public static void addLines(WidgetHolder widgets, Component first, Component second, int width, int height,
                                int offsetX) {
        Font font = Minecraft.getInstance().font;
        int y = height - BLOCK_HEIGHT;
        widgets.addText(first, offsetX + (width - font.width(first)) / 2, y, COLOR, false);
        widgets.addText(second, offsetX + (width - font.width(second)) / 2, y + LINE_HEIGHT, COLOR, false);
    }

}
