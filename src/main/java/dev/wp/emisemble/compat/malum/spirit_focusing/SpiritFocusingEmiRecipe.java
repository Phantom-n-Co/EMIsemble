package dev.wp.emisemble.compat.malum.spirit_focusing;

import com.sammy.malum.common.recipe.SpiritFocusingRecipe;
import com.sammy.malum.core.systems.recipe.SpiritIngredient;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** One spirit focusing page: the spirit row across the top, the input above the arrow, the result below it. */
final class SpiritFocusingEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    private static final int SPIRIT_X = 56;
    private static final int SPIRIT_Y = 8;
    private static final int INPUT_X = 58;
    private static final int INPUT_Y = 53;
    private static final int OUTPUT_X = 58;
    private static final int OUTPUT_Y = 120;

    private final RecipeHolder<SpiritFocusingRecipe> holder;
    private final List<EmiIngredient> spirits;
    private final EmiIngredient input;
    private final EmiStack output;

    SpiritFocusingEmiRecipe(RecipeHolder<SpiritFocusingRecipe> holder) {
        super(SpiritFocusingCompat.CATEGORY, holder.id(), SpiritFocusingCompat.WIDTH, SpiritFocusingCompat.HEIGHT);
        SpiritFocusingRecipe recipe = holder.value();
        this.holder = holder;
        this.input = EmiIngredient.of(recipe.input);
        this.output = EmiStack.of(recipe.output);

        this.spirits = new ArrayList<>(recipe.spirits.size());
        for (SpiritIngredient spirit : recipe.spirits) {
            // asItemStack carries the spirit's own shard count, which is what the host's slot shows.
            EmiIngredient slot = EmiStack.of(spirit.asItemStack());
            spirits.add(slot);
            inputs.add(slot);
        }
        inputs.add(input);
        outputs.add(output);
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, SpiritFocusingCompat.PAGE, SpiritFocusingCompat.SHEET,
                -4, -4, SpiritFocusingCompat.WIDTH + 8, SpiritFocusingCompat.HEIGHT + 8, 1, 0);
        if (!spirits.isEmpty()) {
            sockets(widgets, spirits.size());
        }

        for (int i = 0; i < spirits.size(); i++) {
            RecipeSlots.add(widgets, spirits.get(i), leftOfRow(spirits.size()) + 18 * i, SPIRIT_Y);
        }
        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }

    private static int leftOfRow(int slots) {
        return SPIRIT_X - 9 * (slots - 1);
    }

    /** Centred on the spirit row: one 18x22 socket per spirit between two 2x22 end caps. */
    private static void sockets(WidgetHolder widgets, int slots) {
        int left = leftOfRow(slots);
        for (int i = 0; i < slots; i++) {
            AlphaTextures.add(widgets, SpiritFocusingCompat.SOCKETS, SpiritFocusingCompat.SOCKET_SHEET,
                    left - 1 + 18 * i, SPIRIT_Y - 3, 18, 22, 2, 0);
        }
        AlphaTextures.add(widgets, SpiritFocusingCompat.SOCKETS, SpiritFocusingCompat.SOCKET_SHEET,
                left - 3, SPIRIT_Y - 3, 2, 22, 0, 0);
        AlphaTextures.add(widgets, SpiritFocusingCompat.SOCKETS, SpiritFocusingCompat.SOCKET_SHEET,
                left - 1 + 18 * slots, SPIRIT_Y - 3, 2, 22, 20, 0);
    }
}
