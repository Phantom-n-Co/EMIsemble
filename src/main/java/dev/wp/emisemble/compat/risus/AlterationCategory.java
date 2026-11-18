package dev.wp.emisemble.compat.risus;

import com.bigdious.risus.init.RisusBlocks;
import com.bigdious.risus.init.RisusRecipes;
import com.bigdious.risus.inventory.recipe.AlterationRecipe;
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
import net.minecraft.world.item.crafting.RecipeHolder;

/** {@code risus:alteration}: one ingredient in, one item out, under the alteration catalyst. */
public final class AlterationCategory {
    public static final String NAMESPACE = "risus";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RisusRecipes.ALTERATION_RECIPE.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RisusBlocks.ALTERATION_CATALYST.get()), "gui.risus.alteration_jei");

    private AlterationCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RisusBlocks.ALTERATION_CATALYST.get()));

        int added = EmiRecipes.addAll(registry, RisusRecipes.ALTERATION_RECIPE.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} alteration recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/alteration_gui.png");
        // The host blits a 116x54 crop out of a 256x256 sheet, so the art is opaque but carries no card of its own:
        // EMI's frame stays, and both slot frames are already painted into the art.
        private static final int SHEET = 256;
        private static final int WIDTH = 116;
        private static final int HEIGHT = 54;
        private static final int INPUT_X = 19;
        private static final int INPUT_Y = 19;
        private static final int OUTPUT_X = 81;
        private static final int OUTPUT_Y = 19;

        private final EmiIngredient input;
        private final EmiStack output;

        private Page(RecipeHolder<AlterationRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            AlterationRecipe recipe = holder.value();
            this.input = EmiIngredient.of(recipe.input());
            this.output = EmiStack.of(recipe.result());

            if (!input.isEmpty()) {
                inputs.add(input);
            }
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            // The art's rounded corners are transparent, and EMI's frame shows through them.
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
            if (!input.isEmpty()) {
                RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
            }
            if (!output.isEmpty()) {
                RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
            }
        }
    }
}
