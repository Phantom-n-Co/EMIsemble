package dev.wp.emisemble.compat.sfm.printing_press;

import ca.teamdman.sfm.common.recipe.PrintingPressRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;

/** One printing press page: the form, the ink and the paper, then the printed copies. */
public final class PrintingPressEmiRecipe extends BasicEmiRecipe {
    private final EmiIngredient form;
    private final EmiIngredient ink;
    private final EmiIngredient paper;

    PrintingPressEmiRecipe(ResourceLocation id, PrintingPressRecipe recipe) {
        super(PrintingPressCategory.CATEGORY, id, PrintingPressCategory.WIDTH, PrintingPressCategory.HEIGHT);
        this.form = EmiIngredient.of(recipe.form());
        this.ink = EmiIngredient.of(recipe.ink());
        this.paper = EmiIngredient.of(recipe.paper());

        inputs.add(form);
        inputs.add(ink);
        inputs.add(paper);
        outputs.addAll(form.getEmiStacks());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // The host frames these three slots with the standard slot background and leaves the output bare.
        RecipeSlots.addFramed(widgets, form, 0, 0);
        RecipeSlots.addFramed(widgets, ink, 0, 18);
        RecipeSlots.addFramed(widgets, paper, 0, 36);
        RecipeSlots.add(widgets, form, 25, 18).recipeContext(this);
    }
}
