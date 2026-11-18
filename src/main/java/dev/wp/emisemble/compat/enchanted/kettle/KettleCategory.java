package dev.wp.emisemble.compat.enchanted.kettle;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.favouriteless.enchanted.common.init.EItems;
import net.favouriteless.enchanted.common.init.ERecipeTypes;
import net.favouriteless.enchanted.common.recipes.KettleRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** {@code enchanted:kettle}: up to six ingredients brewed in the kettle for a power cost. */
public final class KettleCategory {
    public static final String NAMESPACE = "enchanted";
    public static final ResourceLocation ID = EmiRecipes.categoryId(ERecipeTypes.KETTLE.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(EItems.KETTLE.get()), "jei.category.enchanted.kettle");

    private KettleCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(EItems.KETTLE.get()));

        int added = EmiRecipes.addAll(registry, ERecipeTypes.KETTLE.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} kettle recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei/witch_cauldron.png");
        private static final int SHEET = 256;

        private static final int WIDTH = 140;
        private static final int HEIGHT = 70;
        private static final int U = 4;
        private static final int V = 4;

        private static final int INPUT_X = 5;
        private static final int INPUT_Y = 5;
        private static final int INPUT_STEP = 20;
        private static final int OUTPUT_X = 110;
        private static final int OUTPUT_Y = 30;

        private static final int ARROW_X = 85;
        private static final int ARROW_Y = 29;
        private static final int TIME_MS = 6000;

        private static final int POWER_Y = 55;
        private static final int POWER_COLOR = 0xFF404040;
        // The host hardcodes this prefix rather than translating it.
        private static final String POWER_PREFIX = "Required Altar Power : ";

        private final List<EmiStack> ingredients;
        private final EmiStack output;
        private final int power;

        private Page(RecipeHolder<KettleRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            KettleRecipe recipe = holder.value();

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
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, U, V);
            widgets.addFillingArrow(ARROW_X, ARROW_Y, TIME_MS);

            for (int i = 0; i < ingredients.size(); i++) {
                RecipeSlots.add(widgets, ingredients.get(i), INPUT_X + INPUT_STEP * i, INPUT_Y);
            }
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);

            Component text = Component.literal(POWER_PREFIX + power);
            widgets.addText(text, WIDTH / 2 - Minecraft.getInstance().font.width(text) / 2, POWER_Y,
                    POWER_COLOR, false);
        }
    }
}
