package dev.wp.emisemble.compat.toxony.mortar_pestle;

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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import xyz.yfrostyf.toxony.recipes.MortarPestleRecipe;
import xyz.yfrostyf.toxony.registries.BlockRegistry;
import xyz.yfrostyf.toxony.registries.ItemRegistry;
import xyz.yfrostyf.toxony.registries.RecipeRegistry;

/** {@code toxony:mortar_pestle}: up to four reagents ground into one output. */
public final class MortarPestleCategory {
    public static final String NAMESPACE = "toxony";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RecipeRegistry.MORTAR_PESTLE_RECIPE.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(ItemRegistry.MORTAR_PESTLE.get()), "block.toxony.mortar_pestle");

    private MortarPestleCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(BlockRegistry.MORTAR_PESTLE.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(BlockRegistry.REDSTONE_MORTAR.get()));

        int added = EmiRecipes.addAll(registry, RecipeRegistry.MORTAR_PESTLE_RECIPE.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} mortar_pestle recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei/mortar_pestle_menu_jei.png");
        private static final int SHEET = 256;

        // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
        private static final int WIDTH = 143;
        private static final int HEIGHT = 57;

        // The host places the reagents by index, two to a row, and the art leaves the spare cells empty.
        private static final int GRID_X = 17;
        private static final int GRID_Y = 12;
        private static final int GRID_STEP = 18;
        private static final int SECOND_ROW_Y = 30;
        private static final int OUTPUT_X = 116;
        private static final int OUTPUT_Y = 21;

        private final EmiStack output;

        private Page(RecipeHolder<MortarPestleRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            MortarPestleRecipe recipe = holder.value();

            for (Ingredient ingredient : recipe.getIngredients()) {
                inputs.add(EmiIngredient.of(ingredient));
            }

            this.output = EmiStack.of(recipe.getResultItem(null));
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, -4, -4, WIDTH + 8, HEIGHT + 8, 12, 9);

            for (int i = 0; i < inputs.size(); i++) {
                RecipeSlots.add(widgets, inputs.get(i), GRID_X + i % 2 * GRID_STEP, i > 1 ? SECOND_ROW_Y : GRID_Y);
            }
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }
    }
}
