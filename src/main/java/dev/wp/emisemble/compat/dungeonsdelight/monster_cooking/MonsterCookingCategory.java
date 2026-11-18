package dev.wp.emisemble.compat.dungeonsdelight.monster_cooking;

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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.yirmiri.dungeonsdelight.common.block.monster_pot.MonsterPotRecipe;
import net.yirmiri.dungeonsdelight.core.registry.DDBlocks;
import net.yirmiri.dungeonsdelight.core.registry.DDItems;
import net.yirmiri.dungeonsdelight.core.registry.DDRecipeRegistries;

import java.util.ArrayList;
import java.util.List;

/** {@code dungeonsdelight:monster_cooking}: up to six ingredients simmered into one dish. */
public final class MonsterCookingCategory {
    public static final String NAMESPACE = "dungeonsdelight";
    public static final ResourceLocation ID = EmiRecipes.categoryId(DDRecipeRegistries.MONSTER_COOKING_RECIPE_TYPE.get());
    // The host titles this through farmers-delight's own keys, so the port must not add a title of its own.
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(DDItems.MONSTER_POT.get()), "jei.farmersdelight.jei.monster_cooking");

    private MonsterCookingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(DDBlocks.MONSTER_POT.get().asItem()));

        int added = EmiRecipes.addAll(registry, DDRecipeRegistries.MONSTER_COOKING_RECIPE_TYPE.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} monster_cooking recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/monster_pot_jei.png");
        private static final int SHEET = 256;
        // The host's createDrawable(29, 16, 116, 56): the page art is that region, not the sheet corner.
        private static final int ART_U = 29;
        private static final int ART_V = 16;

        // The art is the whole page: a flat panel whose slot frames sit on its edge, so JEI's coordinates carry over.
        private static final int WIDTH = 116;
        private static final int HEIGHT = 56;

        private static final int HEAT_X = 18;
        private static final int HEAT_Y = 39;
        private static final int HEAT_WIDTH = 17;
        private static final int HEAT_HEIGHT = 15;
        private static final int HEAT_U = 176;
        private static final int HEAT_V = 0;

        private static final int ARROW_X = 60;
        private static final int ARROW_Y = 9;
        private static final int ARROW_WIDTH = 24;
        private static final int ARROW_HEIGHT = 17;
        private static final int ARROW_U = 176;
        private static final int ARROW_V = 15;
        private static final int ARROW_TICKS = 200;

        private static final int TIME_X = 64;
        private static final int TIME_Y = 2;
        private static final int TIME_WIDTH = 8;
        private static final int TIME_HEIGHT = 11;
        private static final int TIME_U = 176;
        private static final int TIME_V = 32;

        private static final int EXP_X = 63;
        private static final int EXP_Y = 21;
        private static final int EXP_WIDTH = 9;
        private static final int EXP_HEIGHT = 9;
        private static final int EXP_U = 176;
        private static final int EXP_V = 43;

        private static final int INPUT_START_X = 1;
        private static final int INPUT_START_Y = 1;
        private static final int INPUT_STEP = 18;
        private static final int INPUT_COLUMNS = 3;

        private static final int OUTPUT_X = 95;
        private static final int OUTPUT_Y = 10;
        private static final int OUTPUT_LOWER_Y = 39;
        private static final int CONTAINER_X = 63;
        private static final int CONTAINER_Y = 39;

        private static final int TOOLTIP_X = 61;
        private static final int TOOLTIP_Y = 2;
        private static final int TOOLTIP_WIDTH = 22;
        private static final int TOOLTIP_HEIGHT = 28;

        // Kept index-aligned with the art's six sockets: a recipe with fewer ingredients still lands in its own cell.
        private final List<EmiIngredient> ingredients;
        private final EmiStack output;
        private final EmiStack container;
        private final List<Component> tooltip;
        private final boolean hasExperience;

        private Page(RecipeHolder<MonsterPotRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            MonsterPotRecipe recipe = holder.value();
            ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());

            this.ingredients = new ArrayList<>();
            for (Ingredient ingredient : recipe.getIngredients()) {
                EmiIngredient emiIngredient = EmiIngredient.of(ingredient);
                ingredients.add(emiIngredient);
                if (!emiIngredient.isEmpty()) {
                    inputs.add(emiIngredient);
                }
            }

            this.output = EmiStack.of(result);
            if (!output.isEmpty()) {
                outputs.add(output);
            }
            this.container = EmiStack.of(recipe.getOutputContainer());
            if (!container.isEmpty()) {
                catalysts.add(container);
            }

            this.hasExperience = recipe.getExperience() > 0.0F;
            this.tooltip = new ArrayList<>();
            if (recipe.getCookTime() > 0) {
                tooltip.add(Component.translatable("emi.cooking.time", recipe.getCookTime() / 20));
            }
            if (hasExperience) {
                tooltip.add(Component.translatable("emi.cooking.experience", recipe.getExperience()));
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, ART_U, ART_V);
            // The arrow is the host's own sprite, revealed left to right over the static one painted in the art.
            AlphaTextures.progress(widgets, TEXTURE, SHEET, ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT,
                    ARROW_U, ARROW_V, ARROW_TICKS);
            AlphaTextures.add(widgets, TEXTURE, SHEET, HEAT_X, HEAT_Y, HEAT_WIDTH, HEAT_HEIGHT, HEAT_U, HEAT_V);
            AlphaTextures.add(widgets, TEXTURE, SHEET, TIME_X, TIME_Y, TIME_WIDTH, TIME_HEIGHT, TIME_U, TIME_V);
            if (hasExperience) {
                AlphaTextures.add(widgets, TEXTURE, SHEET, EXP_X, EXP_Y, EXP_WIDTH, EXP_HEIGHT, EXP_U, EXP_V);
            }

            for (int i = 0; i < ingredients.size(); i++) {
                EmiIngredient ingredient = ingredients.get(i);
                int x = INPUT_START_X + i % INPUT_COLUMNS * INPUT_STEP;
                int y = INPUT_START_Y + i / INPUT_COLUMNS * INPUT_STEP;
                if (!ingredient.isEmpty()) {
                    RecipeSlots.add(widgets, ingredient, x, y);
                }
            }

            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
            if (!container.isEmpty()) {
                RecipeSlots.add(widgets, container, CONTAINER_X, CONTAINER_Y);
            }
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_LOWER_Y);

            if (!tooltip.isEmpty()) {
                widgets.addTooltipText(tooltip, TOOLTIP_X, TOOLTIP_Y, TOOLTIP_WIDTH, TOOLTIP_HEIGHT);
            }
        }
    }
}
