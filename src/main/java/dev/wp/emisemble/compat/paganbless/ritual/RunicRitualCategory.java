package dev.wp.emisemble.compat.paganbless.ritual;

import com.pigdad.paganbless.content.blocks.RuneSlabBlock;
import com.pigdad.paganbless.content.recipes.RunicRitualRecipe;
import com.pigdad.paganbless.registries.PBBlocks;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

/** {@code paganbless:runic_ritual}: a ring of six rune slabs around the runic core yields one item. */
public final class RunicRitualCategory {
    private static final String NAMESPACE = "paganbless";
    // The host registers no recipe type, so its JEI uid is the only id both viewers agree on.
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "runic_ritual");
    // The host titles the category with Component.literal, so there is no translation key to follow.
    private static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID, EmiStack.of(PBBlocks.RUNIC_CORE.get()),
            Component.literal("Runic Ritual"));

    private RunicRitualCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(PBBlocks.RUNIC_CORE.get()));
        addRuneSlabs(registry);

        int added = EmiRecipes.addAll(registry, RunicRitualRecipe.Type.INSTANCE, Page::new);

        Emisemble.LOGGER.info("{}: {} runic ritual recipes registered", NAMESPACE, added);
        return added;
    }

    // The host's catalyst handler accepts any rune slab but the inert one, and there is no registry entry to list them.
    private static void addRuneSlabs(EmiRegistry registry) {
        for (Item item : BuiltInRegistries.ITEM) {
            Block block = Block.byItem(item);
            if (block instanceof RuneSlabBlock && block != PBBlocks.RUNE_SLAB_INERT.get()) {
                registry.addWorkstation(CATEGORY, EmiStack.of(item));
            }
        }
    }

    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/runic_ritual.png");
        private static final int SHEET = 256;
        // JEI declares a 176x85 panel, of which the PNG paints the top 176x81; the page takes the painted rows.
        private static final int ART_WIDTH = 176;
        private static final int ART_HEIGHT = 81;
        // EMI boxes the widgets in a card 4px wider on every side, so the art is blitted 4px out and fills it.
        private static final int INSET = 4;
        private static final int WIDTH = ART_WIDTH - 8;
        private static final int HEIGHT = ART_HEIGHT - 8;

        private static final int[] INPUT_X = {104, 55, 123, 37, 105, 55};
        private static final int[] INPUT_Y = {8, 8, 33, 33, 57, 57};
        private static final int OUTPUT_X = 80;
        private static final int OUTPUT_Y = 37;

        private final EmiStack rune;
        private final EmiStack output;

        private Page(RecipeHolder<RunicRitualRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            RunicRitualRecipe recipe = holder.value();
            this.rune = EmiStack.of(recipe.getRuneBlock());
            this.output = EmiStack.of(recipe.getResultItem(null));
            // Every ring position holds the same rune slab, which is the only ingredient the recipe names.
            for (int i = 0; i < INPUT_X.length; i++) {
                inputs.add(rune);
            }
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, -INSET, -INSET, ART_WIDTH, ART_HEIGHT, 0, 0);
            for (int i = 0; i < INPUT_X.length; i++) {
                RecipeSlots.add(widgets, rune, INPUT_X[i] - INSET, INPUT_Y[i] - INSET);
            }
            RecipeSlots.add(widgets, output, OUTPUT_X - INSET, OUTPUT_Y - INSET).recipeContext(this);
        }
    }
}
