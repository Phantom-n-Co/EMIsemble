package dev.wp.emisemble.compat.malum.extras;

import com.sammy.malum.core.handlers.hiding.HiddenTagHandler;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.mixin.EmiStackListInvoker;
import dev.wp.emisemble.porting.EmiVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Malum hides item tags until the player sees a revelation, so visibility changes mid-session. Recipes filter
 * live;
 */
final class MalumVisibility {
    private static boolean pending;
    private static boolean installing;

    private MalumVisibility() {}

    static void install() {
        try {
            EmiVisibility.hideStacks(MalumVisibility::hidden);
            EmiVisibility.hideRecipes(MalumVisibility::hidden);
            installing = true;
            try {
                // The host runs the listener once immediately; the bake for this pass is already under way.
                HiddenTagHandler.registerHiddenItemListener(MalumVisibility::refilterIndex);
            } finally {
                installing = false;
            }
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("malum: could not install the hiding predicates", t);
        }
    }

    private static void refilterIndex() {
        if (pending || installing) {
            return;
        }
        pending = true;
        Minecraft.getInstance().execute(() -> {
            pending = false;
            try {
                EmiStackListInvoker.emiNatives$bakeFiltered();
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("malum: re-filtering the EMI index after a hiding change failed", t);
            }
        });
    }

    private static boolean hidden(EmiStack stack) {
        ItemStack item = stack.getItemStack();
        return !item.isEmpty() && HiddenTagHandler.isHiddenItem(item);
    }

    private static boolean hidden(EmiRecipe recipe) {
        // Malum hides its own pages; an unrelated recipe that happens to use a hidden item must stay.
        if (recipe.getId() == null || !recipe.getId().getNamespace().equals("malum")) {
            return false;
        }
        for (var ingredient : recipe.getInputs()) {
            for (EmiStack stack : ingredient.getEmiStacks()) {
                if (hidden(stack)) {
                    return true;
                }
            }
        }
        for (var ingredient : recipe.getCatalysts()) {
            for (EmiStack stack : ingredient.getEmiStacks()) {
                if (hidden(stack)) {
                    return true;
                }
            }
        }
        for (EmiStack stack : recipe.getOutputs()) {
            if (hidden(stack)) {
                return true;
            }
        }
        return false;
    }
}
