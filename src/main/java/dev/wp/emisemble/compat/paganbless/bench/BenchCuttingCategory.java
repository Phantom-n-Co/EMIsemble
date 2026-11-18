package dev.wp.emisemble.compat.paganbless.bench;

import com.pigdad.paganbless.content.recipes.BenchCuttingRecipe;
import com.pigdad.paganbless.registries.PBBlocks;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
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
import net.minecraft.world.item.crafting.RecipeHolder;

/** {@code paganbless:bench_cutting}: one herb cut with one tool, for a fixed number of uses. */
public final class BenchCuttingCategory {
    private static final String NAMESPACE = "paganbless";
    // The host registers no recipe type, so its JEI uid is the only id both viewers agree on.
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "bench_cutting");
    // The host titles the category with Component.literal, so there is no translation key to follow.
    private static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID, EmiStack.of(PBBlocks.HERBALIST_BENCH.get()),
            Component.literal("Bench Cutting"));

    private BenchCuttingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(PBBlocks.HERBALIST_BENCH.get()));

        int added = EmiRecipes.addAll(registry, BenchCuttingRecipe.Type.INSTANCE, Page::new);

        Emisemble.LOGGER.info("{}: {} bench cutting recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/herbalist_bench.png");
        private static final int SHEET = 256;
        // JEI declares a 176x85 panel, of which the PNG paints the top 176x81; the page takes the painted rows.
        private static final int ART_WIDTH = 176;
        private static final int ART_HEIGHT = 81;
        // The host measures its count text against the declared panel height, not the painted rows.
        private static final int PANEL_HEIGHT = 85;
        // EMI boxes the widgets in a card 4px wider on every side, so the art is blitted 4px out and fills it.
        private static final int INSET = 4;
        private static final int WIDTH = ART_WIDTH - 8;
        private static final int HEIGHT = ART_HEIGHT - 8;

        private static final int INPUT_X = 35;
        private static final int INPUT_Y = 33;
        private static final int TOOL_X = 79;
        private static final int TOOL_Y = 56;
        private static final int OUTPUT_X = 125;
        private static final int OUTPUT_Y = 33;
        // The host right-aligns the count against the panel's edge, 10px in and 20px up.
        private static final int TEXT_RIGHT = ART_WIDTH - 10 - INSET;
        private static final int TEXT_Y = PANEL_HEIGHT - 20 - INSET;

        private final EmiIngredient input;
        private final EmiIngredient tool;
        private final EmiStack output;
        private final Component cuts;

        private Page(RecipeHolder<BenchCuttingRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            BenchCuttingRecipe recipe = holder.value();
            this.input = IngredientAmounts.of(recipe.ingredient());
            this.tool = EmiIngredient.of(recipe.toolItem());
            this.output = EmiStack.of(recipe.getResultItem(null));
            this.cuts = Component.translatable("jei_category.paganbless.bench_cutting.cuts", recipe.cuts());
            inputs.add(input);
            inputs.add(tool);
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, -INSET, -INSET, ART_WIDTH, ART_HEIGHT, 0, 0);
            RecipeSlots.add(widgets, input, INPUT_X - INSET, INPUT_Y - INSET);
            RecipeSlots.add(widgets, tool, TOOL_X - INSET, TOOL_Y - INSET);
            RecipeSlots.add(widgets, output, OUTPUT_X - INSET, OUTPUT_Y - INSET).recipeContext(this);
            widgets.addText(cuts, TEXT_RIGHT, TEXT_Y, 0xFFFFFF, false).horizontalAlign(TextWidget.Alignment.END);
        }
    }
}
