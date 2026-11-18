package dev.wp.emisemble.compat.sfm.falling_anvil;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Random;

/** The disenchanting page: any carrier holding one enchantment, and the book that comes off it. */
final class FallingAnvilDisenchantPage extends BasicEmiRecipe {
    private final List<EmiStack> items;
    private final List<EmiStack> books;
    private final EmiIngredient blankBook;
    private final EmiIngredient disenchanting;
    private final int unique;

    FallingAnvilDisenchantPage(List<EmiStack> items, List<EmiStack> books, EmiIngredient disenchanting) {
        super(FallingAnvilCategory.CATEGORY,
                EmiRecipes.synthetic(ResourceLocation.fromNamespaceAndPath("sfm", "falling_anvil/disenchant")),
                FallingAnvilCategory.WIDTH, FallingAnvilCategory.HEIGHT);
        this.items = items;
        this.books = books;
        this.blankBook = EmiIngredient.of(Ingredient.of(Items.BOOK));
        this.disenchanting = disenchanting;
        this.unique = getId().hashCode();

        inputs.add(EmiIngredient.of(items));
        inputs.add(blankBook);
        catalysts.add(FallingAnvilCategory.ANVILS);
        catalysts.add(disenchanting);
        outputs.addAll(books);
    }

    // Each supplier draws one index from the same seed, which keeps the carrier and its book in step.
    private EmiStack item(Random random) {
        return items.get(random.nextInt(items.size()));
    }

    private EmiStack book(Random random) {
        return books.get(random.nextInt(books.size()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        RecipeSlots.add(widgets, FallingAnvilCategory.ANVILS, 8, 0);
        RecipeSlots.add(widgets, disenchanting, 8, 36);
        RecipeSlots.add(widgets, blankBook, 18, 18);
        // The page has no art of its own, so both cycling slots keep the host's bare look.
        RecipeSlots.addGenerated(widgets, this::item, unique, 0, 18).drawBack(false);
        RecipeSlots.addGenerated(widgets, this::book, unique, 50, 18).drawBack(false).recipeContext(this);
    }
}
