package dev.wp.emisemble.compat.cookingforblockheads;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.cookingforblockheads.cow_jar.CowJarCategory;
import net.blay09.mods.cookingforblockheads.client.gui.screen.KitchenScreen;
import net.minecraft.client.gui.components.Button;

/** Cooking for Blockheads' single recipe category, plus the kitchen screen area its sort buttons occupy. */
public final class CookingForBlockheadsCompat {
    private CookingForBlockheadsCompat() {}

    static void register(EmiRegistry registry) {
        kitchenSortButtons(registry);
        int added = CowJarCategory.register(registry);
        Emisemble.LOGGER.info("cookingforblockheads: {} cow_jar recipes registered", added);
    }

    // The host reserves these so a viewer panel cannot cover them; it has no recipe click areas.
    private static void kitchenSortButtons(EmiRegistry registry) {
        try {
            registry.addExclusionArea(KitchenScreen.class, (screen, add) -> {
                for (Button button : screen.getSortingButtons()) {
                    add.accept(new Bounds(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
                }
            });
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("cookingforblockheads: could not reserve the kitchen sort buttons", t);
        }
    }
}
