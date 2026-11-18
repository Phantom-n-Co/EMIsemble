package dev.wp.emisemble.compat.apotheosis.stoneforming;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** The affix's name, its whole candidate set as one cycling input, and an arrow into this page's share of it. */
final class StoneformingEmiRecipe extends BasicEmiRecipe {
    static final int WIDTH = 140;
    static final int HEIGHT = 86;

    private static final int COLUMNS = 4;
    private static final int VISIBLE_ROWS = 4;
    static final int PAGE_SIZE = COLUMNS * VISIBLE_ROWS;

    private static final int PITCH = 18;
    private static final int GRID_X = 53;
    private static final int GRID_Y = 15;

    private static final int INPUT_X = 2;
    private static final int INPUT_Y = 42;
    private static final int ARROW_X = 23;
    private static final int ARROW_Y = 41;

    private static final int TITLE_Y = 2;
    private static final int TITLE_COLOUR = 0xFF404040;

    private static final String HINT_KEY = "info.apotheosis.stoneforming";
    private static final String TARGET_KEY = "info.apotheosis.stoneforming.target";

    private final Component title;
    private final EmiIngredient input;
    private final List<EmiStack> candidates;

    StoneformingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, Component title, EmiIngredient input,
                          List<EmiStack> candidates) {
        super(category, id, WIDTH, HEIGHT);
        this.title = title;
        this.input = input;
        this.candidates = List.copyOf(candidates);

        if (!input.isEmpty()) {
            this.inputs.add(input);
        }
        this.outputs.addAll(this.candidates);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // JEI centres in a 140-wide area from x = 0 and EMI centres on the x it is given, so both anchor the middle.
        widgets.addText(this.title, WIDTH / 2, TITLE_Y, TITLE_COLOUR, false)
                .horizontalAlign(TextWidget.Alignment.CENTER);

        // The host's page has no background art, so these slots keep EMI's frame: addFramed is the right call here.
        SlotWidget slot = RecipeSlots.addFramed(widgets, this.input, INPUT_X, INPUT_Y);
        slot.appendTooltip(Component.translatable(HINT_KEY, this.title).withStyle(ChatFormatting.GRAY));
        slot.appendTooltip(Component.translatable(TARGET_KEY).withStyle(ChatFormatting.GRAY));

        // An affix whose candidate tag is empty gets no arrow, exactly as the host draws it.
        if (!this.candidates.isEmpty()) {
            widgets.addTexture(EmiTexture.EMPTY_ARROW, ARROW_X, ARROW_Y);
        }

        // Each cell is one of the page's outputs, so it carries the context: this page is the only place EMI can learn
        // which set an affix draws its blocks from.
        for (int i = 0; i < this.candidates.size(); i++) {
            RecipeSlots.addFramed(widgets, this.candidates.get(i),
                    GRID_X + (i % COLUMNS) * PITCH, GRID_Y + (i / COLUMNS) * PITCH).recipeContext(this);
        }
    }
}
