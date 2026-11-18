package dev.wp.emisemble.compat.toxony.alembic;

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
import xyz.yfrostyf.toxony.recipes.AlembicRecipe;
import xyz.yfrostyf.toxony.registries.BlockRegistry;
import xyz.yfrostyf.toxony.registries.ItemRegistry;
import xyz.yfrostyf.toxony.registries.RecipeRegistry;

/** {@code toxony:alembic}: a reagent plus a solvent, distilled into one output. */
public final class AlembicCategory {
    public static final String NAMESPACE = "toxony";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RecipeRegistry.ALEMBIC_RECIPE.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(ItemRegistry.ALEMBIC.get()), "block.toxony.alembic");

    private AlembicCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(BlockRegistry.ALEMBIC.get()));

        int added = EmiRecipes.addAll(registry, RecipeRegistry.ALEMBIC_RECIPE.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} alembic recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei/alembic_menu_jei.png");
        private static final ResourceLocation BUBBLES =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/sprites/container/bubbles.png");
        private static final ResourceLocation PROGRESS =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/sprites/container/progress_bar.png");
        private static final int SHEET = 256;

        // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
        private static final int WIDTH = 116;
        private static final int HEIGHT = 63;

        private static final int INPUT_X = 44;
        private static final int INPUT_Y = 42;
        private static final int SOLVENT_X = 80;
        private static final int SOLVENT_Y = 37;
        private static final int OUTPUT_X = 80;
        private static final int OUTPUT_Y = 2;

        private static final int BUBBLES_X = 19;
        private static final int BUBBLES_Y = 24;
        private static final int PROGRESS_X = 107;
        private static final int PROGRESS_Y = 32;
        private static final int BUBBLE_TICKS = 14;
        private static final int BAR_TICKS = 300;

        private final EmiIngredient input;
        private final EmiIngredient solvent;
        private final EmiStack output;

        private Page(RecipeHolder<AlembicRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            AlembicRecipe recipe = holder.value();

            this.input = EmiIngredient.of(recipe.getIngredient());
            this.solvent = EmiIngredient.of(recipe.getIngredientToConvert());
            this.output = EmiStack.of(recipe.getResultItem(null));

            inputs.add(input);
            inputs.add(solvent);
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, -4, -4, WIDTH + 8, HEIGHT + 8, 29, 5);

            // Bubbles rise while the bar fills: the host's timer counts down, which grows both bottom-anchored slices.
            AlphaTextures.rising(widgets, BUBBLES, BUBBLES_X, BUBBLES_Y, 10, 27, 0, 0, BUBBLE_TICKS, false);
            AlphaTextures.rising(widgets, PROGRESS, PROGRESS_X, PROGRESS_Y, 3, 25, 0, 0, BAR_TICKS, false);

            RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
            RecipeSlots.add(widgets, solvent, SOLVENT_X, SOLVENT_Y);
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }
    }
}
