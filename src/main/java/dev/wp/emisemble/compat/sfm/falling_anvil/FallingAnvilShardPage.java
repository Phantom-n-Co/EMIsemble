package dev.wp.emisemble.compat.sfm.falling_anvil;

import ca.teamdman.sfm.common.registry.registration.SFMItems;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** The shard page: an enchanted book dropped onto a disenchanting block yields an experience shard. */
final class FallingAnvilShardPage extends BasicEmiRecipe {
    private final EmiIngredient book;
    private final EmiIngredient disenchanting;
    private final EmiStack shard;

    FallingAnvilShardPage(EmiIngredient disenchanting) {
        super(FallingAnvilCategory.CATEGORY,
                EmiRecipes.synthetic(ResourceLocation.fromNamespaceAndPath("sfm", "falling_anvil/experience_shard")),
                FallingAnvilCategory.WIDTH, FallingAnvilCategory.HEIGHT);
        this.book = EmiIngredient.of(Ingredient.of(Items.ENCHANTED_BOOK));
        this.disenchanting = disenchanting;
        this.shard = EmiStack.of(SFMItems.EXPERIENCE_SHARD.get());

        inputs.add(book);
        catalysts.add(FallingAnvilCategory.ANVILS);
        catalysts.add(disenchanting);
        outputs.add(shard);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        RecipeSlots.add(widgets, FallingAnvilCategory.ANVILS, 0, 0);
        RecipeSlots.add(widgets, book, 0, 18);
        RecipeSlots.add(widgets, disenchanting, 0, 36);
        RecipeSlots.add(widgets, shard, 50, 18).recipeContext(this);
    }
}
