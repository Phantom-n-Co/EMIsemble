package dev.wp.emisemble.porting;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.List;

/** Inputs on a circle around a centre, the way Ars' apparatus pages place their pedestals. */
public final class RingSlots {
    private RingSlots() {}

    /** Steps by 360/n from (jeiX, jeiY), keeping the float accumulator JEI uses so the same slot lands on the same pixel. */
    public static void add(WidgetHolder widgets, List<EmiIngredient> inputs, int jeiX, int jeiY, int centreX,
                           int centreY) {
        if (inputs.isEmpty()) {
            return;
        }
        double step = Math.toRadians(360.0 / inputs.size());
        double x = jeiX;
        double y = jeiY;
        // The pages that use this draw no art, so the ring keeps EMI's frame instead of floating.
        for (EmiIngredient input : inputs) {
            RecipeSlots.addFramed(widgets, input, (int) x, (int) y);
            double nextX = Math.cos(step) * (x - centreX) - Math.sin(step) * (y - centreY) + centreX;
            y = Math.sin(step) * (x - centreX) + Math.cos(step) * (y - centreY) + centreY;
            x = nextX;
        }
    }
}
