package dev.wp.emisemble.compat.immersiveengineering.arcfurnace;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.immersiveengineering.IeText;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** One arc furnace page, recycling included: the host draws no art, only the arrow, the frames and the slots. */
public final class ArcFurnaceEmiRecipe extends BasicEmiRecipe {
    private static final int BASE_WIDTH = 86;
    private static final int SECOND_OUTPUT_COLUMN = 18;
    private static final int CHANCE_COLUMN = 40;
    private static final int SECONDARY_OFFSET = 88;
    private static final int SECONDARY_OFFSET_WIDE = 106;
    private static final int ADDITIVE_SOCKETS = 4;

    private final EmiIngredient input;
    private final List<EmiIngredient> additives = new ArrayList<>();
    private final List<EmiStack> base = new ArrayList<>();
    private final List<EmiStack> secondary = new ArrayList<>();
    private final EmiStack slag;
    private final List<Component> arrowTooltip;

    private final int x;
    private final int frameX;
    private final int secondaryX;

    public ArcFurnaceEmiRecipe(EmiRecipeCategory category, ResourceLocation id, ArcFurnaceRecipe recipe) {
        super(category, id, ArcFurnaceCategory.WIDTH, ArcFurnaceCategory.HEIGHT);

        this.input = EmiIngredient.of(recipe.input.getBaseIngredient(), recipe.input.getCount());
        for (IngredientWithSize additive : recipe.additives) {
            additives.add(EmiIngredient.of(additive.getBaseIngredient(), additive.getCount()));
        }
        for (ItemStack stack : recipe.getBaseOutputs()) {
            if (!stack.isEmpty()) {
                base.add(EmiStack.of(stack));
            }
        }
        for (StackWithChance entry : recipe.secondaryOutputs) {
            ItemStack stack = entry.stack().get();
            if (stack.isEmpty()) {
                continue;
            }
            // EMI styles chance slots and states the odds in its tooltip, so the host's percent text is dropped.
            secondary.add(EmiStack.of(stack).setChance(entry.chance()));
        }
        this.slag = EmiStack.of(recipe.slag.get());

        int width = BASE_WIDTH
                + (base.size() > 1 ? SECOND_OUTPUT_COLUMN : 0)
                + (secondary.isEmpty() ? 0 : CHANCE_COLUMN);
        this.x = (ArcFurnaceCategory.WIDTH - width) / 2 + 1;
        this.frameX = x - 1;
        this.secondaryX = x + (base.size() > 1 ? SECONDARY_OFFSET_WIDE : SECONDARY_OFFSET);

        // The arrow is the host's only source of the process cost: hovering it states IF/tick and the duration.
        int time = recipe.getTotalProcessTime();
        int energy = recipe.getTotalProcessEnergy();
        this.arrowTooltip = List.of(
                IeText.energyPerTick(time > 0 ? (float) energy / time : 0),
                IeText.seconds(time));

        inputs.add(input);
        inputs.addAll(additives);
        outputs.addAll(base);
        outputs.addAll(secondary);
        if (!slag.isEmpty()) {
            outputs.add(slag);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(ArcFurnaceCategory.ARROW_TEXTURE, frameX + ArcFurnaceCategory.ARROW_X,
                ArcFurnaceCategory.ARROW_Y, ArcFurnaceCategory.ARROW_WIDTH, ArcFurnaceCategory.ARROW_HEIGHT,
                ArcFurnaceCategory.ARROW_U, ArcFurnaceCategory.ARROW_V, ArcFurnaceCategory.ARROW_WIDTH,
                ArcFurnaceCategory.ARROW_HEIGHT, ArcFurnaceCategory.ARROW_TEXTURE_SIZE,
                ArcFurnaceCategory.ARROW_TEXTURE_SIZE);
        widgets.addTooltipText(arrowTooltip, frameX + ArcFurnaceCategory.ARROW_X, 8,
                ArcFurnaceCategory.ARROW_WIDTH, ArcFurnaceCategory.ARROW_HEIGHT);

        RecipeSlots.addFramed(widgets, input, x + 8, 1);

        // The host paints all four additive sockets whether or not the recipe uses them, which is how the page shows
        // the machine's additive capacity.
        for (int j = 0; j < ADDITIVE_SOCKETS; j++) {
            emptyCell(widgets, x + (j % 2) * 18, 19 + (j / 2) * 18);
        }
        for (int j = 0; j < additives.size(); j++) {
            RecipeSlots.addFramed(widgets, additives.get(j), x + (j % 2) * 18, 19 + (j / 2) * 18);
        }

        for (int j = 0; j < base.size(); j++) {
            RecipeSlots.addFramed(widgets, base.get(j), x + 68 + (j % 2) * 18, (j / 2) * 18 + 1)
                    .recipeContext(this);
        }

        for (int j = 0; j < secondary.size(); j++) {
            RecipeSlots.addFramed(widgets, secondary.get(j), secondaryX, j * 18 + 1).recipeContext(this);
        }

        if (slag.isEmpty()) {
            emptyCell(widgets, x + 68, 37);
        } else {
            RecipeSlots.addFramed(widgets, slag, x + 68, 37).recipeContext(this);
        }
    }

    /** A framed socket with nothing in it, for the cells the host draws unconditionally. */
    private static void emptyCell(WidgetHolder widgets, int jeiX, int jeiY) {
        widgets.addSlot(jeiX - 1, jeiY - 1);
    }
}
