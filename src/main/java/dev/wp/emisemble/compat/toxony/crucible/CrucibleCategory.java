package dev.wp.emisemble.compat.toxony.crucible;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import xyz.yfrostyf.toxony.recipes.CrucibleRecipe;
import xyz.yfrostyf.toxony.registries.BlockRegistry;
import xyz.yfrostyf.toxony.registries.ItemRegistry;
import xyz.yfrostyf.toxony.registries.RecipeRegistry;

/** {@code toxony:crucible}: one reagent melted under a falling fuel flame. */
public final class CrucibleCategory {
    public static final String NAMESPACE = "toxony";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RecipeRegistry.CRUCIBLE_RECIPE.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(ItemRegistry.COPPER_CRUCIBLE.get()), "block.toxony.copper_crucible");

    private CrucibleCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(BlockRegistry.COPPER_CRUCIBLE.get()));

        int added = EmiRecipes.addAll(registry, RecipeRegistry.CRUCIBLE_RECIPE.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} crucible recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei/copper_crucible_menu_jei.png");
        private static final ResourceLocation FLAMES =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/sprites/container/crucible_flames.png");
        private static final ResourceLocation PROGRESS =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/sprites/container/progress_bar.png");
        private static final int SHEET = 256;

        // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
        private static final int WIDTH = 109;
        private static final int HEIGHT = 63;

        private static final int INPUT_X = 42;
        private static final int INPUT_Y = 12;
        private static final int OUTPUT_X = 90;
        private static final int OUTPUT_Y = 12;

        private static final int PROGRESS_X = 77;
        private static final int PROGRESS_Y = 8;
        private static final int FLAMES_X = 44;
        private static final int FLAMES_Y = 35;
        private static final int BAR_TICKS = 300;
        private static final int FLAME_TICKS = 400;

        private final EmiIngredient input;
        private final EmiStack output;

        private Page(RecipeHolder<CrucibleRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            CrucibleRecipe recipe = holder.value();

            // The host lays out the first ingredient only, whatever the recipe's list holds.
            this.input = EmiIngredient.of(recipe.getIngredients().getFirst());
            this.output = EmiStack.of(recipe.getResultItem(null));

            if (!input.isEmpty()) {
                inputs.add(input);
            }
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, -4, -4, WIDTH + 8, HEIGHT + 8, 34, 5);

            // Both sprites drain here: the crucible bar counts up and the flames are drawn from TOP inverted.
            AlphaTextures.rising(widgets, PROGRESS, PROGRESS_X, PROGRESS_Y, 3, 25, 0, 0, BAR_TICKS, true);
            AlphaTextures.rising(widgets, FLAMES, FLAMES_X, FLAMES_Y, 13, 11, 0, 0, FLAME_TICKS, true);

            if (!input.isEmpty()) {
                RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
            }
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }
    }
}
