package dev.wp.emisemble.compat.sfm.falling_anvil;

import ca.teamdman.sfm.common.recipe.PrintingPressRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;

/** The forming page: the form, the block that forms it, and the copies that come out of the anvil. */
final class FallingAnvilFormPage extends BasicEmiRecipe {
    private final EmiIngredient form;
    private final EmiIngredient block;

    FallingAnvilFormPage(ResourceLocation parentId, PrintingPressRecipe parent, EmiIngredient block) {
        super(FallingAnvilCategory.CATEGORY, EmiRecipes.synthetic(ResourceLocation.fromNamespaceAndPath(
                        parentId.getNamespace(), "falling_anvil/form/" + parentId.getPath())),
                FallingAnvilCategory.WIDTH, FallingAnvilCategory.HEIGHT);
        this.form = EmiIngredient.of(parent.form());
        this.block = block;

        inputs.add(form);
        inputs.add(block);
        catalysts.add(FallingAnvilCategory.ANVILS);
        outputs.addAll(form.getEmiStacks());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        RecipeSlots.add(widgets, FallingAnvilCategory.ANVILS, 0, 0);
        RecipeSlots.add(widgets, form, 0, 18);
        RecipeSlots.add(widgets, block, 0, 36);
        RecipeSlots.add(widgets, form, 50, 18).recipeContext(this);
    }
}
