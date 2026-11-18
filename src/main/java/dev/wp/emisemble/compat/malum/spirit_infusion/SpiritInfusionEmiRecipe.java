package dev.wp.emisemble.compat.malum.spirit_infusion;

import com.sammy.malum.common.recipe.SpiritInfusionRecipe;
import com.sammy.malum.core.systems.recipe.SpiritIngredient;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * One spirit infusion page: the spirit run down the left, the extra ingredients down the right, input over output.
 */
final class SpiritInfusionEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    private static final int SPIRIT_X = 15;
    private static final int EXTRA_X = 99;
    private static final int COLUMN_Y = 45;
    private static final int INPUT_X = 58;
    private static final int INPUT_Y = 53;
    private static final int OUTPUT_X = 58;
    private static final int OUTPUT_Y = 120;
    // Past five ingredients the host moves the column's origin down ten pixels per slot, which is what keeps a run of
    // eight spirits on the page instead of hanging off its top edge.
    private static final int COLUMN_LIMIT = 5;
    private static final int COLUMN_SHIFT = 10;

    private final RecipeHolder<SpiritInfusionRecipe> holder;
    private final List<EmiIngredient> spirits;
    private final List<EmiIngredient> extras;
    private final EmiIngredient input;
    private final EmiStack output;

    SpiritInfusionEmiRecipe(RecipeHolder<SpiritInfusionRecipe> holder) {
        super(SpiritInfusionCompat.CATEGORY, holder.id(), SpiritInfusionCompat.WIDTH, SpiritInfusionCompat.HEIGHT);
        SpiritInfusionRecipe recipe = holder.value();
        this.holder = holder;
        this.input = EmiIngredient.of(recipe.input.ingredient(), recipe.input.count());
        this.output = EmiStack.of(recipe.result);

        this.spirits = new ArrayList<>(recipe.spirits.size());
        for (SpiritIngredient spirit : recipe.spirits) {
            // A spirit is an item plus a count, so the shard stack is the slot: no stack type can say more than this.
            EmiIngredient slot = EmiStack.of(spirit.asItemStack());
            spirits.add(slot);
            inputs.add(slot);
        }
        this.extras = new ArrayList<>(recipe.extraInputs.size());
        for (SizedIngredient extra : recipe.extraInputs) {
            EmiIngredient slot = EmiIngredient.of(extra.ingredient(), extra.count());
            extras.add(slot);
            inputs.add(slot);
        }
        inputs.add(input);
        if (!output.isEmpty()) {
            outputs.add(output);
        }
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, SpiritInfusionCompat.TEXTURE, SpiritInfusionCompat.SHEET,
                -4, -4, SpiritInfusionCompat.WIDTH + 8, SpiritInfusionCompat.HEIGHT + 8, 1, 0);
        sockets(widgets, SPIRIT_X, spirits.size());
        sockets(widgets, EXTRA_X, extras.size());

        int spiritY = firstSlotY(spirits.size());
        for (int i = 0; i < spirits.size(); i++) {
            RecipeSlots.add(widgets, spirits.get(i), SPIRIT_X, spiritY + 18 * i);
        }
        int extraY = firstSlotY(extras.size());
        for (int i = 0; i < extras.size(); i++) {
            RecipeSlots.add(widgets, extras.get(i), EXTRA_X, extraY + 18 * i);
        }
        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }

    /** The column's own origin before the host centres the run on it: columns past the limit start lower. */
    private static int columnBase(int slots) {
        return COLUMN_Y + (slots > COLUMN_LIMIT ? (slots - COLUMN_LIMIT) * COLUMN_SHIFT : 0);
    }

    private static int firstSlotY(int slots) {
        return columnBase(slots) - 9 * (slots - 1);
    }

    /** A vertical run of the codex socket sprite: one 22x18 per slot, closed by two 22x2 end caps. */
    private static void sockets(WidgetHolder widgets, int x, int slots) {
        if (slots == 0) {
            return;
        }
        int top = firstSlotY(slots);
        for (int i = 0; i < slots; i++) {
            AlphaTextures.add(widgets, SpiritInfusionCompat.SOCKETS, SpiritInfusionCompat.SOCKET_SHEET,
                    x - 3, top - 1 + 18 * i, 22, 18, 0, 2);
        }
        AlphaTextures.add(widgets, SpiritInfusionCompat.SOCKETS, SpiritInfusionCompat.SOCKET_SHEET,
                x - 3, top - 3, 22, 2, 0, 0);
        AlphaTextures.add(widgets, SpiritInfusionCompat.SOCKETS, SpiritInfusionCompat.SOCKET_SHEET,
                x - 3, top - 1 + 18 * slots, 22, 2, 0, 20);
    }
}
