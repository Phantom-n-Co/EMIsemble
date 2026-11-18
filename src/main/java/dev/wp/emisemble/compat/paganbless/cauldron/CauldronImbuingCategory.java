package dev.wp.emisemble.compat.paganbless.cauldron;

import com.pigdad.paganbless.content.recipes.ImbuingCauldronRecipe;
import com.pigdad.paganbless.registries.PBBlocks;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.IngredientAmounts;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

/** {@code paganbless:cauldron_imbuing}: up to five items and an optional fluid imbued into one result. */
public final class CauldronImbuingCategory {
    private static final String NAMESPACE = "paganbless";
    // The host registers no recipe type, so its JEI uid is the only id both viewers agree on.
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "cauldron_imbuing");
    // The host titles the category with Component.literal, so there is no translation key to follow.
    private static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID, EmiStack.of(PBBlocks.IMBUING_CAULDRON.get()),
            Component.literal("Cauldron Imbuing"));

    private CauldronImbuingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(PBBlocks.IMBUING_CAULDRON.get()));

        int added = EmiRecipes.addAll(registry, ImbuingCauldronRecipe.Type.INSTANCE, Page::new);

        Emisemble.LOGGER.info("{}: {} cauldron imbuing recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/imbuing_cauldron.png");
        private static final int SHEET = 256;
        // JEI declares a 176x85 panel, of which the PNG paints the top 176x81; the page takes the painted rows.
        private static final int ART_WIDTH = 176;
        private static final int ART_HEIGHT = 81;
        // EMI boxes the widgets in a card 4px wider on every side, so the art is blitted 4px out and fills it.
        private static final int INSET = 4;
        private static final int WIDTH = ART_WIDTH - 8;
        private static final int HEIGHT = ART_HEIGHT - 8;

        private static final int[] INPUT_X = {76, 119, 102, 49, 32};
        private static final int[] INPUT_Y = {6, 20, 59, 59, 20};
        private static final int OUTPUT_X = 76;
        private static final int OUTPUT_Y = 38;
        // The art's tank socket, 17x18 with a 16-row interior, which the fill is measured against here.
        private static final int TANK_X = 148;
        private static final int TANK_Y = 51;
        private static final int TANK_WIDTH = 17;
        private static final int TANK_HEIGHT = 18;
        // JEI draws the tank 34px tall for 2000 mB, so its 17px slot is 1000 mB and the painted window fills at that.
        private static final int TANK_CAPACITY = 1000;

        private final List<EmiIngredient> ingredients;
        private final EmiIngredient fluid;
        private final EmiStack output;

        private Page(RecipeHolder<ImbuingCauldronRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            ImbuingCauldronRecipe recipe = holder.value();
            this.ingredients = recipe.ingredients().stream().map(IngredientAmounts::of).toList();
            this.fluid = recipe.fluidIngredient().map(FluidTanks::of).orElse(EmiStack.EMPTY);
            this.output = EmiStack.of(recipe.getResultItem(null));
            inputs.addAll(ingredients);
            if (!fluid.isEmpty()) {
                inputs.add(fluid);
            }
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, -INSET, -INSET, ART_WIDTH, ART_HEIGHT, 0, 0);
            if (!fluid.isEmpty()) {
                FluidTanks.addExact(widgets, fluid, TANK_X - INSET, TANK_Y - INSET, TANK_WIDTH, TANK_HEIGHT,
                        TANK_CAPACITY);
            }

            // The host always lays out five slots and leaves the ones a recipe has no ingredient for empty.
            for (int i = 0; i < INPUT_X.length; i++) {
                if (i < ingredients.size()) {
                    RecipeSlots.add(widgets, ingredients.get(i), INPUT_X[i] - INSET, INPUT_Y[i] - INSET);
                } else {
                    RecipeSlots.addEmpty(widgets, INPUT_X[i] - INSET, INPUT_Y[i] - INSET);
                }
            }
            RecipeSlots.add(widgets, output, OUTPUT_X - INSET, OUTPUT_Y - INSET).recipeContext(this);
        }
    }
}
