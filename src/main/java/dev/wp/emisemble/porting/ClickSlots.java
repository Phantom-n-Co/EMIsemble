package dev.wp.emisemble.porting;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

/** Slot moves through the menu's own click handling, which is how a remote screen can move items at all. */
public final class ClickSlots {
    private ClickSlots() {}

    /** The whole stack; a merge hands the remainder back, a swap trades the two slots. */
    public static void move(AbstractContainerMenu menu, Slot from, Slot to) {
        click(menu, from, 0, ClickType.PICKUP);
        click(menu, to, 0, ClickType.PICKUP);
        click(menu, from, 0, ClickType.PICKUP);
    }

    /** Exactly {@code count} items; the rest of the source stack stays where it was. */
    public static void moveCount(AbstractContainerMenu menu, Slot from, Slot to, int count) {
        click(menu, from, 0, ClickType.PICKUP);
        for (int i = 0; i < count; i++) {
            click(menu, to, 1, ClickType.PICKUP);
        }
        click(menu, from, 0, ClickType.PICKUP);
    }

    public static void pickup(AbstractContainerMenu menu, Slot slot) {
        click(menu, slot, 0, ClickType.PICKUP);
    }

    public static void quickMove(AbstractContainerMenu menu, Slot slot) {
        click(menu, slot, 0, ClickType.QUICK_MOVE);
    }

    private static void click(AbstractContainerMenu menu, Slot slot, int button, ClickType type) {
        Minecraft client = Minecraft.getInstance();
        if (client.gameMode == null || client.player == null) {
            return;
        }
        client.gameMode.handleInventoryMouseClick(menu.containerId, slot.index, button, type, client.player);
    }
}
