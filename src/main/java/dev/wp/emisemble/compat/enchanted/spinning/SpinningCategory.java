package dev.wp.emisemble.compat.enchanted.spinning;

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
import net.favouriteless.enchanted.common.init.EItems;
import net.favouriteless.enchanted.common.init.ERecipeTypes;
import net.favouriteless.enchanted.common.recipes.SpinningRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** {@code enchanted:spinning}: three items wound together on the spinning wheel, plus the altar power it costs. */
public final class SpinningCategory {
    public static final String NAMESPACE = "enchanted";
    // The recipe type is registered as enchanted:wheel while JEI files the category as enchanted:spinning; JEMI
    // matches on the JEI uid, so the literal is what suppresses its shim.
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "spinning");
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(EItems.SPINNING_WHEEL.get()), "container.enchanted.spinning_wheel");

    private SpinningCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(EItems.SPINNING_WHEEL.get()));

        int added = EmiRecipes.addAll(registry, ERecipeTypes.SPINNING.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} spinning recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/menus/spinning_wheel.png");
        private static final int SHEET = 256;

        // The art is 140x60 and the page 140x75: the bottom band is left for the power line, as the host drew it.
        private static final int WIDTH = 140;
        private static final int HEIGHT = 75;
        private static final int U = 20;
        private static final int V = 10;
        private static final int ART_HEIGHT = 60;

        private static final int[] INPUT_X = {25, 13, 37};
        private static final int[] INPUT_Y = {13, 37, 37};
        private static final int OUTPUT_X = 110;
        private static final int OUTPUT_Y = 25;

        private static final int LEFT_ARROW_X = 7;
        private static final int RIGHT_ARROW_X = 44;
        private static final int ARROW_Y = 16;
        private static final int ARROW_W = 15;
        private static final int ARROW_H = 20;
        private static final int TIME_MS = 6000;

        private static final int POWER_Y = 65;
        private static final int POWER_COLOR = 0xFF404040;
        // The host hardcodes this prefix rather than translating it.
        private static final String POWER_PREFIX = "Required Altar Power : ";

        private final List<EmiStack> ingredients;
        private final EmiStack output;
        private final int power;

        private Page(RecipeHolder<SpinningRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            SpinningRecipe recipe = holder.value();

            this.ingredients = recipe.getInputs().stream().map(EmiStack::of).toList();
            this.output = EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
            this.power = recipe.getPower();

            inputs.addAll(ingredients);
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, ART_HEIGHT, U, V);
            // JEI fills each arrow upward from its base over 120 ticks; EMI's bottom-anchored crop does the same unwound.
            widgets.addAnimatedTexture(TEXTURE, LEFT_ARROW_X, ARROW_Y, ARROW_W, ARROW_H, 176, 0, ARROW_W, ARROW_H,
                    SHEET, SHEET, TIME_MS, false, true, false);
            widgets.addAnimatedTexture(TEXTURE, RIGHT_ARROW_X, ARROW_Y, ARROW_W, ARROW_H, 176, 20, ARROW_W, ARROW_H,
                    SHEET, SHEET, TIME_MS, false, true, false);

            for (int i = 0; i < ingredients.size() && i < INPUT_X.length; i++) {
                RecipeSlots.add(widgets, ingredients.get(i), INPUT_X[i], INPUT_Y[i]);
            }
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);

            Component text = Component.literal(POWER_PREFIX + power);
            widgets.addText(text, WIDTH / 2 - Minecraft.getInstance().font.width(text) / 2, POWER_Y,
                    POWER_COLOR, false);
        }
    }
}
