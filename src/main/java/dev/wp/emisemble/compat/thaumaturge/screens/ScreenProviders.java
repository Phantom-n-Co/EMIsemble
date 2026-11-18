package dev.wp.emisemble.compat.thaumaturge.screens;

import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget;
import com.leclowndu93150.thaumaturge.client.screen.casters.focal.FocalManipulatorScreen;
import com.leclowndu93150.thaumaturge.client.screen.research.EntryDetailScreen;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.wp.emisemble.compat.thaumaturge.AspectEmiStack;

/** The thaumonomicon's lookup chips, and the focal manipulator opting out of EMI overlays. */
public final class ScreenProviders {
    private ScreenProviders() {}

    public static void register(EmiRegistry registry) {
        registry.addStackProvider(EntryDetailScreen.class, ScreenProviders::stackUnderMouse);
        // JEI's extra area for the whole screen; EMI's version of "do not draw over this GUI".
        registry.addExclusionArea(FocalManipulatorScreen.class,
                (screen, consumer) -> consumer.accept(new Bounds(0, 0, screen.width, screen.height)));
    }

    // Item first, then aspect: the screen already refuses to report an aspect the player has not researched.
    private static EmiStackInteraction stackUnderMouse(EntryDetailScreen screen, int mouseX, int mouseY) {
        RecipeDisplayWidget.ItemHit item = screen.itemUnderMouse(mouseX, mouseY);
        if (item != null && !item.stack().isEmpty()) {
            return new EmiStackInteraction(EmiStack.of(item.stack()));
        }

        EntryDetailScreen.AspectHit aspect = screen.aspectUnderMouse(mouseX, mouseY);
        if (aspect != null) {
            return new EmiStackInteraction(new AspectEmiStack(aspect.aspect().aspect(), aspect.aspect().amount()));
        }

        return EmiStackInteraction.EMPTY;
    }
}
