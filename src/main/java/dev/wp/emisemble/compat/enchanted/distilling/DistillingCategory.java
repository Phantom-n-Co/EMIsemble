package dev.wp.emisemble.compat.enchanted.distilling;

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
import net.favouriteless.enchanted.common.recipes.DistillingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** {@code enchanted:distilling}: a jar plus two reagents through the distillery, yielding up to four results. */
public final class DistillingCategory {
    public static final String NAMESPACE = "enchanted";
    public static final ResourceLocation ID = EmiRecipes.categoryId(ERecipeTypes.DISTILLING.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(EItems.DISTILLERY.get()), "container.enchanted.distillery");

    private DistillingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(EItems.DISTILLERY.get()));

        int added = EmiRecipes.addAll(registry, ERecipeTypes.DISTILLING.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} distilling recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/menus/distillery.png");
        private static final int SHEET = 256;

        private static final int WIDTH = 146;
        private static final int HEIGHT = 75;
        private static final int U = 4;
        private static final int V = 5;

        private static final int JAR_X = 28;
        private static final int JAR_Y = 30;
        private static final int INPUT_X = 50;
        private static final int INPUT_Y = 20;
        private static final int INPUT_STEP = 20;
        private static final int OUTPUT_X = 123;
        private static final int OUTPUT_Y = 2;
        private static final int OUTPUT_STEP = 19;

        private static final int BUBBLES_X = 88;
        private static final int BUBBLES_Y = 22;
        private static final int BUBBLES_W = 12;
        private static final int BUBBLES_H = 29;
        private static final int STILL_X = 65;
        private static final int STILL_Y = 8;
        private static final int STILL_W = 57;
        private static final int STILL_H = 61;
        private static final int TIME_MS = 6000;

        private final EmiStack jar;
        private final List<EmiStack> reagents = new ArrayList<>();
        private final List<EmiStack> results = new ArrayList<>();

        private Page(RecipeHolder<DistillingRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            DistillingRecipe recipe = holder.value();

            List<ItemStack> stacks = recipe.inputs();
            // The host shows the first input in the jar slot, then columns every input that is not a clay jar.
            boolean jarred = stacks.stream().anyMatch(stack -> stack.is(EItems.CLAY_JAR.get()));
            this.jar = jarred ? EmiStack.of(stacks.getFirst()) : EmiStack.EMPTY;
            for (ItemStack stack : stacks) {
                if (!stack.is(EItems.CLAY_JAR.get())) {
                    reagents.add(EmiStack.of(stack));
                }
            }
            for (ItemStack output : recipe.outputs()) {
                results.add(EmiStack.of(output));
            }

            if (!jar.isEmpty()) {
                inputs.add(jar);
            }
            inputs.addAll(reagents);
            outputs.addAll(results);
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, U, V);
            widgets.addAnimatedTexture(TEXTURE, BUBBLES_X, BUBBLES_Y, BUBBLES_W, BUBBLES_H, 176, 0, BUBBLES_W,
                    BUBBLES_H, SHEET, SHEET, TIME_MS, false, true, false);
            widgets.addAnimatedTexture(TEXTURE, STILL_X, STILL_Y, STILL_W, STILL_H, 176, 29, STILL_W, STILL_H, SHEET,
                    SHEET, TIME_MS, true, false, false);

            if (!jar.isEmpty()) {
                RecipeSlots.add(widgets, jar, JAR_X, JAR_Y);
            }
            for (int i = 0; i < reagents.size(); i++) {
                RecipeSlots.add(widgets, reagents.get(i), INPUT_X, INPUT_Y + INPUT_STEP * i);
            }
            for (int i = 0; i < results.size(); i++) {
                RecipeSlots.add(widgets, results.get(i), OUTPUT_X, OUTPUT_Y + OUTPUT_STEP * i)
                        .recipeContext(this);
            }
        }
    }
}
