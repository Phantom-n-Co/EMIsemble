package dev.wp.emisemble.compat.eidolon.worktable;

import alexthw.eidolon_repraised.recipe.WorktableRecipe;
import alexthw.eidolon_repraised.registries.EidolonRecipes;
import alexthw.eidolon_repraised.registries.Registry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** {@code eidolon_repraised:worktable}: a 3x3 core grid, the four reagent sockets and the result below the arrow. */
public final class WorktableCategory {
    public static final ResourceLocation ID = EmiRecipes.categoryId(EidolonRecipes.WORKTABLE_TYPE.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(Registry.WORKTABLE.get()), "jei.eidolon_repraised.worktable");

    private static final ResourceLocation PAGE =
            ResourceLocation.fromNamespaceAndPath("eidolon_repraised", "textures/gui/jei_page_bg.png");
    private static final ResourceLocation ART =
            ResourceLocation.fromNamespaceAndPath("eidolon_repraised", "textures/gui/codex_worktable_page.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 138;
    private static final int HEIGHT = 172;
    private static final int CORE_X = 44;
    private static final int CORE_Y = 37;
    private static final int CORE_STEP = 17;
    private static final int[] REAGENT_X = {61, 100, 61, 22};
    private static final int[] REAGENT_Y = {15, 54, 93, 54};
    private static final int OUTPUT_X = 61;
    private static final int OUTPUT_Y = 133;

    private WorktableCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Registry.WORKTABLE.get()));

        int added = EmiRecipes.addAll(registry, EidolonRecipes.WORKTABLE_TYPE.get(), WorktableEmiRecipe::new);

        Emisemble.LOGGER.info("eidolon_repraised: {} worktable recipes registered", added);
        return added;
    }

    private static final class WorktableEmiRecipe extends BasicEmiRecipe {
        private final RecipeHolder<WorktableRecipe> holder;
        private final EmiStack output;

        private WorktableEmiRecipe(RecipeHolder<WorktableRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            this.holder = holder;
            WorktableRecipe recipe = holder.value();
            List<Ingredient> core = recipe.getCore();
            List<Ingredient> reagents = recipe.getOuter();
            int width = recipe.pattern_core.width();
            int height = recipe.pattern_core.height();

            // Padded to the 3x3 the menu owns, so input i lands in menu slot i + 1 and a 2-wide pattern keeps its shape.
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int index = row * width + col;
                    Ingredient ingredient = row < height && col < width && index < core.size()
                            ? core.get(index) : Ingredient.EMPTY;
                    inputs.add(EmiIngredient.of(ingredient));
                }
            }
            for (Ingredient reagent : reagents) {
                inputs.add(EmiIngredient.of(reagent));
            }
            this.output = EmiStack.of(recipe.getResult());
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public RecipeHolder<?> getBackingRecipe() {
            return holder;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, PAGE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
            AlphaTextures.add(widgets, ART, SHEET, 5, 4, 128, 160, 0, 0);
            for (int cell = 0; cell < 9; cell++) {
                EmiIngredient input = inputs.get(cell);
                if (!input.isEmpty()) {
                    RecipeSlots.add(widgets, input, CORE_X + cell % 3 * CORE_STEP, CORE_Y + cell / 3 * CORE_STEP);
                }
            }
            for (int reagent = 0; reagent < Math.min(REAGENT_X.length, inputs.size() - 9); reagent++) {
                EmiIngredient input = inputs.get(9 + reagent);
                if (!input.isEmpty()) {
                    RecipeSlots.add(widgets, input, REAGENT_X[reagent], REAGENT_Y[reagent]);
                }
            }
            if (!output.isEmpty()) {
                RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
            }
        }
    }
}
