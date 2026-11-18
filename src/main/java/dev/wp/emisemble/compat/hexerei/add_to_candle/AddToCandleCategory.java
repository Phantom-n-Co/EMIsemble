package dev.wp.emisemble.compat.hexerei.add_to_candle;

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
import net.joefoxe.hexerei.data.recipes.AddToCandleRecipe;
import net.joefoxe.hexerei.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

/** {@code hexerei:add_to_candle}: a candle plus one reagent, on the 144x86 nine-cell art. */
public final class AddToCandleCategory {
    // AddToCandleRecipe#getType() answers CRAFTING, so the pages live in the vanilla list and the uid is not a type key.
    public static final String NAMESPACE = "hexerei";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "add_to_candle");
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/add_to_candle_gui_jei.png");
    public static final EmiRecipeCategory CATEGORY =
            new TranslatedCategory(ID, EmiStack.of(ModItems.CANDLE.get()), "Adding to Candles");

    private AddToCandleCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);

        int added = 0;
        for (RecipeHolder<CraftingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!(holder.value() instanceof AddToCandleRecipe recipe)) {
                continue;
            }
            // The construction is inside the try as well: a throw escaping register costs the plugin's whole set.
            try {
                if (EmiRecipes.add(registry, new Page(ResourceLocation.fromNamespaceAndPath(NAMESPACE, "/" + holder.id().getPath()), recipe))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping hexerei:add_to_candle recipe {}", holder.id(), t);
            }
        }

        Emisemble.LOGGER.info("{}: {} add_to_candle recipes registered", NAMESPACE, added);
        return added;
    }

    /** The host adds a slot only for the ingredients a recipe has; the empty cells are painted on the page. */
    private static final class Page extends BasicEmiRecipe implements OwnFrameRecipe {
        // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
        private static final int WIDTH = 136;
        private static final int HEIGHT = 78;

        private static final int[] COLUMNS = {11, 29, 47};
        private static final int[] ROWS = {15, 33, 51};
        private static final int CELLS = COLUMNS.length * ROWS.length;

        private static final int OUTPUT_X = 105;
        private static final int OUTPUT_Y = 33;

        private static final int TITLE_X = 7;
        private static final int TITLE_Y = 5;
        private static final int TITLE_MAX_WIDTH = 131;
        private static final int TITLE_COLOR = 0xFF404040;
        private static final float LINE_HEIGHT = 4.5F;

        private final List<EmiIngredient> grid;
        private final EmiStack output;
        private final Component title;

        private Page(ResourceLocation id, AddToCandleRecipe recipe) {
            super(CATEGORY, id, WIDTH, HEIGHT);

            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            this.grid = new ArrayList<>(Math.min(ingredients.size(), CELLS));
            for (Ingredient ingredient : ingredients) {
                if (grid.size() == CELLS) {
                    break;
                }
                EmiIngredient slot = EmiIngredient.of(ingredient);
                grid.add(slot);
                inputs.add(slot);
            }

            this.output = EmiStack.of(recipe.getOutput());
            outputs.add(output);
            this.title = titleOf(ingredients, recipe.getOutput());
        }

        /** The host reads the first ingredient unguarded; fall back to the output rather than lose the page. */
        private static Component titleOf(NonNullList<Ingredient> ingredients, ItemStack output) {
            if (!ingredients.isEmpty()) {
                ItemStack[] items = ingredients.get(0).getItems();
                if (items.length > 0) {
                    return items[0].getHoverName();
                }
            }
            return output.getHoverName();
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, 256, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);

            for (int i = 0; i < grid.size(); i++) {
                RecipeSlots.add(widgets, grid.get(i), COLUMNS[i % COLUMNS.length], ROWS[i / COLUMNS.length]);
            }
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);

            // addText cannot scale, and the host squeezes a long candle name with the pose instead.
            widgets.addDrawable(-4, -4, WIDTH, 14, (draw, mouseX, mouseY, delta) -> {
                Font font = Minecraft.getInstance().font;
                FormattedCharSequence text = title.getVisualOrderText();
                int width = font.width(text);
                if (width <= TITLE_MAX_WIDTH) {
                    draw.drawString(font, text, TITLE_X, TITLE_Y, TITLE_COLOR, false);
                    return;
                }
                float percent = width / (float) TITLE_MAX_WIDTH;
                draw.pose().pushPose();
                draw.pose().scale(1.0F / percent, 1.0F / percent, 1.0F);
                draw.drawString(font, text, TITLE_X * percent, (TITLE_Y + LINE_HEIGHT) * percent - LINE_HEIGHT,
                        TITLE_COLOR, false);
                draw.pose().popPose();
            });
        }
    }
}
