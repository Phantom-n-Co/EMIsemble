package dev.wp.emisemble.compat.toxony.alchemical_forge;

import com.mojang.datafixers.util.Pair;
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
import xyz.yfrostyf.toxony.api.affinity.Affinity;
import xyz.yfrostyf.toxony.recipes.AlchemicalForgeRecipe;
import xyz.yfrostyf.toxony.registries.BlockRegistry;
import xyz.yfrostyf.toxony.registries.ItemRegistry;
import xyz.yfrostyf.toxony.registries.RecipeRegistry;

import java.util.List;

/** {@code toxony:alchemical_forge}: a main reagent and two auxiliaries combined under three affinities. */
public final class AlchemicalForgeCategory {
    public static final String NAMESPACE = "toxony";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RecipeRegistry.ALCHEMICAL_FORGE_RECIPE.get());
    // The host's own icon is this part item, and it has no model in 0.10.7: JEI draws the missing-texture checker too.
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(ItemRegistry.ALCHEMICAL_FORGE_PART.get()), "block.toxony.alchemical_forge");

    private AlchemicalForgeCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(BlockRegistry.ALCHEMICAL_FORGE_PART.get()));

        int added = EmiRecipes.addAll(registry, RecipeRegistry.ALCHEMICAL_FORGE_RECIPE.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} alchemical_forge recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei/alchemical_forge_menu_jei.png");
        private static final ResourceLocation AFFINITY_ICONS =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/affinity_icons.png");
        private static final int SHEET = 256;

        // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
        private static final int WIDTH = 168;
        private static final int HEIGHT = 113;

        private static final int MAIN_X = 76;
        private static final int MAIN_Y = 39;
        private static final int AUX_ONE_X = 50;
        private static final int AUX_TWO_X = 102;
        private static final int AUX_Y = 39;
        private static final int OUTPUT_X = 76;
        private static final int OUTPUT_Y = 89;

        // The affinity strip is drawn by hand in the host: a centred run of 13px icons 15px apart, 8px closer per extra icon.
        private static final int AFFINITY_SHEET_WIDTH = 52;
        private static final int AFFINITY_SHEET_HEIGHT = 39;
        private static final int AFFINITY_SIZE = 13;
        private static final int AFFINITY_COLUMNS = 4;
        private static final int AFFINITY_ANCHOR_X = 78;
        private static final int AFFINITY_Y = 4;
        private static final int AFFINITY_STEP = 15;
        private static final int AFFINITY_TIGHTEN = 8;

        private final EmiIngredient main;
        private final EmiIngredient auxOne;
        private final EmiIngredient auxTwo;
        private final List<Affinity> affinities;
        private final EmiStack output;

        private Page(RecipeHolder<AlchemicalForgeRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            AlchemicalForgeRecipe recipe = holder.value();
            Pair<Ingredient, Ingredient> aux = recipe.getAuxIngredients();

            this.main = EmiIngredient.of(recipe.getMainIngredient());
            this.auxOne = EmiIngredient.of(aux.getFirst());
            this.auxTwo = EmiIngredient.of(aux.getSecond());
            this.affinities = recipe.getAffinities();
            this.output = EmiStack.of(recipe.getResultItem(null));

            // Auxiliaries may be absent, and the host still adds their slots: an empty socket shows neither item nor frame.
            for (EmiIngredient ingredient : List.of(main, auxOne, auxTwo)) {
                if (!ingredient.isEmpty()) {
                    inputs.add(ingredient);
                }
            }
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);

            int startX = AFFINITY_ANCHOR_X - (affinities.size() - 1) * AFFINITY_TIGHTEN;
            for (int i = 0; i < affinities.size(); i++) {
                int index = affinities.get(i).getIndex();
                // u wraps inside the blit's texture width; v has to index the row by hand.
                AlphaTextures.add(widgets, AFFINITY_ICONS, AFFINITY_SHEET_WIDTH, AFFINITY_SHEET_HEIGHT,
                        startX + i * AFFINITY_STEP, AFFINITY_Y, AFFINITY_SIZE, AFFINITY_SIZE,
                        index * AFFINITY_SIZE, index / AFFINITY_COLUMNS * AFFINITY_SIZE);
            }

            if (!main.isEmpty()) {
                RecipeSlots.add(widgets, main, MAIN_X, MAIN_Y);
            }
            if (!auxOne.isEmpty()) {
                RecipeSlots.add(widgets, auxOne, AUX_ONE_X, AUX_Y);
            }
            if (!auxTwo.isEmpty()) {
                RecipeSlots.add(widgets, auxTwo, AUX_TWO_X, AUX_Y);
            }
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }
    }
}
