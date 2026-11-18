package dev.wp.emisemble.compat.apotheosis.gem_cutting;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.BasicGemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

/** Both gem cutting shapes: same 148x78 art, four input sockets, one output. */
public final class GemCuttingEmiRecipe extends BasicEmiRecipe {
    private static final int BASE_X = 48;
    private static final int TOP_X = 48;
    private static final int LEFT_X = 19;
    private static final int RIGHT_X = 76;
    private static final int SOCKET_Y = 56;
    private static final int BASE_Y = 37;
    private static final int TOP_Y = 4;
    private static final int OUTPUT_X = 117;
    private static final int OUTPUT_Y = 35;
    // Shared by a purity page's three cycling sockets on purpose: EMI seeds a generated slot from time ^ unique.
    private static final int CYCLE_UNIQUE = 1;

    private final RecipeHolder<? extends GemCuttingRecipe> holder;
    private final EmiIngredient base;
    private final EmiIngredient top;
    private final EmiIngredient left;
    private final EmiIngredient right;
    private final EmiIngredient output;
    // The purity a page cycles at, or null for the basic page, whose sockets hold fixed ingredients.
    private final Purity cyclePurity;

    private GemCuttingEmiRecipe(RecipeHolder<? extends GemCuttingRecipe> holder, EmiIngredient base,
                                EmiIngredient top, EmiIngredient left, EmiIngredient right,
                                EmiIngredient output, List<EmiStack> outputs, Purity cyclePurity) {
        super(GemCuttingCompat.CATEGORY, holder.id(), GemCuttingCompat.WIDTH, GemCuttingCompat.HEIGHT);
        this.holder = holder;
        this.base = base;
        this.top = top;
        this.left = left;
        this.right = right;
        this.output = output;
        this.cyclePurity = cyclePurity;
        inputs.addAll(List.of(base, top, left, right));
        this.outputs.addAll(outputs);
    }

    static GemCuttingEmiRecipe basic(RecipeHolder<GemCuttingRecipe> holder, BasicGemCuttingRecipe recipe) {
        EmiStack output = EmiStack.of(recipe.output());
        return new GemCuttingEmiRecipe(holder,
                EmiIngredient.of(recipe.base()),
                GemCuttingIngredients.sized(recipe.top()),
                GemCuttingIngredients.sized(recipe.left()),
                GemCuttingIngredients.sized(recipe.right()),
                output,
                output.isEmpty() ? List.of() : List.of(output),
                null);
    }

    /**
     * The recipe copies the base gem at {@code purity.next()}, so the ingredient lists stay full sets — every gem at
     * the purity is a legal input — while the sockets show one gem at a time and the output shows that same gem upgraded.
     */
    static GemCuttingEmiRecipe purityUpgrade(RecipeHolder<GemCuttingRecipe> holder, PurityUpgradeRecipe recipe) {
        Purity result = recipe.purity().next();
        return new GemCuttingEmiRecipe(holder,
                GemCuttingIngredients.gems(recipe.purity()),
                GemCuttingIngredients.gems(recipe.purity()),
                GemCuttingIngredients.sized(recipe.left()),
                GemCuttingIngredients.sized(recipe.right()),
                GemCuttingIngredients.gems(result),
                GemCuttingIngredients.gemsAt(result),
                recipe.purity());
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, GemCuttingCompat.TEXTURE, GemCuttingCompat.SHEET,
                0, 0, GemCuttingCompat.WIDTH, GemCuttingCompat.HEIGHT, 0, 0);
        if (cyclePurity == null) {
            RecipeSlots.add(widgets, base, BASE_X, BASE_Y);
            RecipeSlots.add(widgets, top, TOP_X, TOP_Y);
        } else {
            // Both input sockets take the same gem, so all three cycling widgets share one unique and draw the same
            // gem on the same tick; the output picks from the input's list so the two can never disagree.
            Function<Random, EmiIngredient> asInput = GemCuttingIngredients.cycle(cyclePurity, cyclePurity);
            generated(widgets, asInput, BASE_X, BASE_Y);
            generated(widgets, asInput, TOP_X, TOP_Y);
        }
        RecipeSlots.add(widgets, left, LEFT_X, SOCKET_Y);
        RecipeSlots.add(widgets, right, RIGHT_X, SOCKET_Y);
        if (cyclePurity == null) {
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        } else {
            generated(widgets, GemCuttingIngredients.cycle(cyclePurity, cyclePurity.next()), OUTPUT_X, OUTPUT_Y)
                    .recipeContext(this);
        }
    }

    /** A cycling socket in JEI coordinates, frameless like {@link RecipeSlots#add}. */
    private static SlotWidget generated(WidgetHolder widgets, Function<Random, EmiIngredient> supplier,
                                        int jeiX, int jeiY) {
        return widgets.addGeneratedSlot(supplier, CYCLE_UNIQUE, jeiX - 1, jeiY - 1).drawBack(false);
    }
}
