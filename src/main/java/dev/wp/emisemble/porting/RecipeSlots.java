package dev.wp.emisemble.porting;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.Random;
import java.util.function.Function;

/**
 * Slots placed with JEI's coordinates: EMI's SlotWidget background is 18x18 where JEI's ingredient rect is 16x16, so every
 * x and y shifts by one. `add` draws no chrome, since most hosts put the slot art in the page texture.
 */
public final class RecipeSlots {
    private RecipeSlots() {}

    /** A frameless slot: the ingredient lands exactly where JEI drew it. */
    public static SlotWidget add(WidgetHolder widgets, EmiIngredient ingredient, int jeiX, int jeiY) {
        return widgets.addSlot(ingredient, jeiX - 1, jeiY - 1).drawBack(false);
    }

    /** An empty grid cell, so a sparse shaped recipe still shows the full grid the host drew. */
    public static SlotWidget addEmpty(WidgetHolder widgets, int jeiX, int jeiY) {
        return widgets.addSlot(jeiX - 1, jeiY - 1).drawBack(false);
    }

    /** For the rare category that has no page art and relies on the viewer's own slot background. */
    public static SlotWidget addFramed(WidgetHolder widgets, EmiIngredient ingredient, int jeiX, int jeiY) {
        return widgets.addSlot(ingredient, jeiX - 1, jeiY - 1);
    }

    /** The empty frame a scroll grid keeps around its last row, so a short page still shows the host's whole grid. */
    public static SlotWidget addFramedEmpty(WidgetHolder widgets, int jeiX, int jeiY) {
        return widgets.addSlot(jeiX - 1, jeiY - 1);
    }

    /** A framed slot that re-rolls its stack every second, for a page whose family shares one slot. */
    public static SlotWidget addGenerated(WidgetHolder widgets, Function<Random, EmiIngredient> supplier, int unique,
                                          int jeiX, int jeiY) {
        return widgets.addGeneratedSlot(supplier, unique, jeiX - 1, jeiY - 1);
    }
}
