package dev.wp.emisemble.compat.paganbless.anvil;

import com.pigdad.paganbless.content.recipes.AnvilSmashingRecipe;
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
import dev.wp.emisemble.porting.IngredientAmounts;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

/** {@code paganbless:anvil_smashing}: up to three inputs smashed on an anvil or under a winch. */
public final class AnvilSmashingCategory {
    private static final String NAMESPACE = "paganbless";
    // The host registers no recipe type, so its JEI uid is the only id both viewers agree on.
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "anvil_smashing");
    // The host titles the category with Component.literal, so there is no translation key to follow.
    private static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID, EmiStack.of(Items.ANVIL),
            Component.literal("Anvil Smashing"));

    private AnvilSmashingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.ANVIL));
        registry.addWorkstation(CATEGORY, EmiStack.of(PBBlocks.WINCH.get()));

        int added = EmiRecipes.addAll(registry, AnvilSmashingRecipe.Type.INSTANCE, Page::new);

        Emisemble.LOGGER.info("{}: {} anvil smashing recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/anvil_smashing.png");
        private static final int SHEET = 256;
        // JEI declares a 176x85 panel, of which the PNG paints the top 176x81; the page takes the painted rows.
        private static final int ART_WIDTH = 176;
        private static final int ART_HEIGHT = 81;
        // EMI boxes the widgets in a card 4px wider on every side, so the art is blitted 4px out and fills it.
        private static final int INSET = 4;
        private static final int WIDTH = ART_WIDTH - 8;
        private static final int HEIGHT = ART_HEIGHT - 8;

        private static final int[] INPUT_X = {79, 52, 105};
        private static final int[] INPUT_Y = {5, 8, 8};
        private static final int OUTPUT_X = 80;
        private static final int OUTPUT_Y = 60;

        private final List<EmiIngredient> ingredients;
        private final EmiStack output;

        private Page(RecipeHolder<AnvilSmashingRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            this.ingredients = holder.value().ingredients().stream().map(IngredientAmounts::of).toList();
            this.output = EmiStack.of(holder.value().getResultItem(null));
            inputs.addAll(ingredients);
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, -INSET, -INSET, ART_WIDTH, ART_HEIGHT, 0, 0);

            // The host always lays out three slots and leaves the ones a recipe has no ingredient for empty.
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
