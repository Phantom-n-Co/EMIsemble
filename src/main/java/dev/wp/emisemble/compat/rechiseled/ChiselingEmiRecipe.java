package dev.wp.emisemble.compat.rechiseled;

import com.supermartijn642.rechiseled.Rechiseled;
import com.supermartijn642.rechiseled.api.chiseling.ItemWithWorth;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** The whole recipe as one cycling input, an arrow, and this page's share of its variants in the host's 9x4 grid. */
final class ChiselingEmiRecipe extends BasicEmiRecipe {
    static final int WIDTH = 178;
    static final int HEIGHT = 94;

    /** One grid cell: the host puts a regular and a connecting variant in the same slot. */
    record Cell(EmiIngredient ingredient, List<Float> worths) {}

    private static final int COLUMNS = 9;
    private static final int VISIBLE_ROWS = 4;
    static final int PAGE_SIZE = COLUMNS * VISIBLE_ROWS;

    private static final int PITCH = 18;
    private static final int GRID_X = 1;
    private static final int GRID_Y = 23;

    // The host repositions its input in createRecipeExtras, away from where the slot was first laid out.
    private static final int INPUT_X = 73;
    private static final int INPUT_Y = 1;
    private static final int ARROW_X = 89;
    private static final int ARROW_Y = 0;

    private static final ResourceLocation ARROW = Rechiseled.identifier("textures/screen/curved_arrow.png");
    private static final ResourceLocation PANEL = Rechiseled.identifier("textures/screen/jei_box_background.png");
    private static final int PANEL_WIDTH = 162;
    private static final int PANEL_HEIGHT = 72;
    private static final String WORTH_KEY = "rechiseled.recipe_category.conversion_value";

    private final EmiIngredient input;
    private final List<Float> inputWorths;
    private final List<Cell> cells;

    ChiselingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, List<ItemWithWorth> inputs, List<Cell> cells) {
        super(category, id, WIDTH, HEIGHT);
        this.input = ingredient(inputs);
        this.inputWorths = worths(inputs);
        this.cells = List.copyOf(cells);
        this.inputs.add(this.input);
        for (Cell cell : this.cells) {
            this.outputs.addAll(cell.ingredient().getEmiStacks());
        }
    }

    static Cell cell(List<ItemWithWorth> items) {
        return new Cell(ingredient(items), worths(items));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // The host's own card behind the grid: a 162x72 dark-outlined panel, drawn under the slot chrome it fills.
        AlphaTextures.add(widgets, PANEL, PANEL_WIDTH, PANEL_HEIGHT, GRID_X - 1, GRID_Y - 1, PANEL_WIDTH, PANEL_HEIGHT,
                0, 0);

        SlotWidget input = RecipeSlots.addFramed(widgets, this.input, INPUT_X, INPUT_Y);
        addWorth(input, this.inputWorths);

        // The host declares its sprite at its own size, which is what keeps a 20x20 file from sampling as 256x256.
        AlphaTextures.add(widgets, ARROW, 20, ARROW_X, ARROW_Y, 20, 20, 0, 0);

        for (int index = 0; index < PAGE_SIZE; index++) {
            int x = GRID_X + (index % COLUMNS) * PITCH;
            int y = GRID_Y + (index / COLUMNS) * PITCH;
            // The host paints all four rows whether or not the page fills them, which is what a short last page shows.
            if (index < this.cells.size()) {
                Cell cell = this.cells.get(index);
                addWorth(RecipeSlots.addFramed(widgets, cell.ingredient(), x, y).recipeContext(this), cell.worths());
            } else {
                RecipeSlots.addFramedEmpty(widgets, x, y);
            }
        }
    }

    private static EmiIngredient ingredient(List<ItemWithWorth> items) {
        return EmiIngredient.of(items.stream().map(item -> EmiStack.of(item.item())).toList());
    }

    /** Distinct values only: a value of one is the default, and the host prints nothing for it. */
    private static List<Float> worths(List<ItemWithWorth> items) {
        List<Float> worths = new ArrayList<>();
        for (ItemWithWorth item : items) {
            float worth = Math.round(item.worth() * 1000.0F) / 1000.0F;
            if (worth != 1.0F && !worths.contains(worth)) {
                worths.add(worth);
            }
        }
        return List.copyOf(worths);
    }

    /** The host prints the value of the hovered stack; an EMI slot can only print the values it holds. */
    private static void addWorth(SlotWidget slot, List<Float> worths) {
        for (float worth : worths) {
            Component value = Component.literal(String.valueOf(worth)).withStyle(ChatFormatting.GOLD);
            slot.appendTooltip(Component.translatable(WORTH_KEY, value).withStyle(ChatFormatting.GRAY));
        }
    }
}
