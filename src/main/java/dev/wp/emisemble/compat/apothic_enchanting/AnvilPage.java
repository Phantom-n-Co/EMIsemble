package dev.wp.emisemble.compat.apothic_enchanting;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;

/** One anvil page — base item, addition, result — in EMI's own anvil layout. */
public final class AnvilPage extends BasicEmiRecipe {
    private static final int WIDTH = 125;
    private static final int HEIGHT = 18;

    private static final int BASE_X = 0;
    private static final int ADDITION_X = 49;
    private static final int RESULT_X = 107;
    private static final int SLOT_Y = 0;
    private static final int PLUS_X = 27;
    private static final int PLUS_Y = 3;
    private static final int ARROW_X = 75;
    private static final int ARROW_Y = 1;

    private final EmiIngredient base;
    private final EmiIngredient addition;
    private final EmiStack result;

    AnvilPage(ResourceLocation id, EmiStack base, EmiIngredient addition, EmiStack result) {
        super(VanillaEmiRecipeCategories.ANVIL_REPAIRING, id, WIDTH, HEIGHT);
        this.base = base;
        this.addition = addition;
        this.result = result;

        inputs.add(base);
        inputs.add(addition);
        outputs.add(result);
    }

    // An anvil page transforms, it does not craft: resolving the tree would nest every tool under every ingredient.
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // EMI's own category draws framed slots in EMI space, so no RecipeSlots -1 shift here.
        widgets.addTexture(EmiTexture.PLUS, PLUS_X, PLUS_Y);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, ARROW_X, ARROW_Y);
        widgets.addSlot(base, BASE_X, SLOT_Y);
        widgets.addSlot(addition, ADDITION_X, SLOT_Y);
        widgets.addSlot(result, RESULT_X, SLOT_Y).recipeContext(this);
    }
}
