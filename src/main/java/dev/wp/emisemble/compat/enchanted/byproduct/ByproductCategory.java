package dev.wp.emisemble.compat.enchanted.byproduct;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.favouriteless.enchanted.common.init.EItems;
import net.favouriteless.enchanted.common.init.ERecipeTypes;
import net.favouriteless.enchanted.common.recipes.ByproductRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** {@code enchanted:byproduct}: the witch oven's extra output for an input, plus what that input would smelt into. */
public final class ByproductCategory {
    public static final String NAMESPACE = "enchanted";
    public static final ResourceLocation ID = EmiRecipes.categoryId(ERecipeTypes.BYPRODUCT.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(EItems.WITCH_OVEN.get()), "container.enchanted.witch_oven");

    private ByproductCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(EItems.WITCH_OVEN.get()));

        int added = EmiRecipes.addAll(registry, ERecipeTypes.BYPRODUCT.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} byproduct recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/menus/witch_oven.png");
        private static final int SHEET = 256;

        private static final int WIDTH = 96;
        private static final int HEIGHT = 65;
        private static final int U = 40;
        private static final int V = 10;

        private static final int INPUT_X = 13;
        private static final int INPUT_Y = 7;
        private static final int SMELTED_X = 67;
        private static final int SMELTED_Y = 7;
        private static final int OUTPUT_X = 67;
        private static final int OUTPUT_Y = 43;
        private static final int JAR_X = 13;
        private static final int JAR_Y = 43;

        private static final int FLAME_X = 40;
        private static final int FLAME_Y = 27;
        private static final int ARROW_X = 36;
        private static final int ARROW_Y = 6;
        // JEI animates both over 120 ticks; EMI's widgets take milliseconds.
        private static final int TIME_MS = 6000;

        private final EmiIngredient input;
        private final List<EmiStack> smelted;
        private final EmiStack output;
        private final EmiStack jar;

        private Page(RecipeHolder<ByproductRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            ByproductRecipe recipe = holder.value();
            ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());

            this.input = EmiIngredient.of(recipe.getInput());
            this.smelted = smeltedFrom(recipe.getInput());
            this.output = EmiStack.of(result);
            this.jar = EmiStack.of(new ItemStack(EItems.CLAY_JAR.get(), result.getCount()));

            inputs.add(input);
            outputs.addAll(smelted);
            if (!output.isEmpty()) {
                outputs.add(output);
            }
            catalysts.add(jar);
        }

        // The host shows what the same item smelts into, so the page answers both lookups from one ingredient.
        private static List<EmiStack> smeltedFrom(Ingredient input) {
            Level level = Minecraft.getInstance().level;
            List<EmiStack> smelted = new ArrayList<>();
            for (ItemStack stack : input.getItems()) {
                Optional<RecipeHolder<SmeltingRecipe>> found = level.getRecipeManager()
                        .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
                found.ifPresent(holder -> smelted.add(
                        EmiStack.of(holder.value().getResultItem(level.registryAccess()))));
            }
            return smelted;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, U, V);
            widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, FLAME_X, FLAME_Y, TIME_MS, false, true, false);
            widgets.addFillingArrow(ARROW_X, ARROW_Y, TIME_MS);

            RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
            if (!smelted.isEmpty()) {
                RecipeSlots.add(widgets, EmiIngredient.of(smelted), SMELTED_X, SMELTED_Y).recipeContext(this);
            }
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
            RecipeSlots.add(widgets, jar, JAR_X, JAR_Y);
        }
    }
}
