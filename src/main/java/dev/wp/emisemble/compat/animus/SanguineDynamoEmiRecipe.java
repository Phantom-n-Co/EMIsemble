package dev.wp.emisemble.compat.animus;

import com.breakinblocks.animusnv.recipes.SanguineDynamoRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** {@code animusnv:sanguine_dynamo}: one orb sets a conversion ratio, and the page states the three rates as text. */
public final class SanguineDynamoEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 160;
    private static final int MIN_HEIGHT = 52;

    // JEI lays the orb out as a 16x16 ingredient at 5,18 and draws its slot frame at 4,17 over a page with no art.
    private static final int ORB_X = 5;
    private static final int ORB_Y = 18;

    private static final int TEXT_X = 28;
    private static final int TEXT_TOP = 6;
    private static final int LINE_STEP = 10;
    // The host draws three fixed lines and lets the widest overflow its 160px page; wrapping to the space the page has.
    private static final int TEXT_WIDTH = WIDTH - TEXT_X - 4;
    private static final int RATIO_COLOR = 0x8B0000;
    private static final int MAX_COLOR = 0x404040;
    private static final int REDSTONE_COLOR = 0x606060;

    private final EmiIngredient orb;
    private final List<FormattedCharSequence> lines = new ArrayList<>();
    private final List<Integer> colors = new ArrayList<>();

    SanguineDynamoEmiRecipe(RecipeHolder<SanguineDynamoRecipe> holder) {
        super(AnimusCompat.SANGUINE_DYNAMO, holder.id(), WIDTH, MIN_HEIGHT);
        SanguineDynamoRecipe recipe = holder.value();

        this.orb = EmiIngredient.of(recipe.orb());
        wrap(Component.translatable("jei.animusnv.sanguine_dynamo.ratio", count(recipe.fePerEv())), RATIO_COLOR);
        wrap(Component.translatable("jei.animusnv.sanguine_dynamo.max",
                count(recipe.maxEvPerTick()), count(recipe.maxFePerTick())), MAX_COLOR);
        wrap(Component.translatable("jei.animusnv.sanguine_dynamo.redstone"), REDSTONE_COLOR);
        this.height = Math.max(MIN_HEIGHT, TEXT_TOP + lines.size() * LINE_STEP + 2);

        if (!orb.isEmpty()) {
            inputs.add(orb);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (!orb.isEmpty()) {
            RecipeSlots.addFramed(widgets, orb, ORB_X, ORB_Y);
        }
        for (int i = 0; i < lines.size(); i++) {
            widgets.addText(lines.get(i), TEXT_X, TEXT_TOP + i * LINE_STEP, colors.get(i), false);
        }
    }

    /** The orb buys a rate and nothing is crafted, so the page stays out of the recipe tree and the craftables list. */
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }

    private void wrap(Component text, int color) {
        for (FormattedCharSequence line : Minecraft.getInstance().font.split(text, TEXT_WIDTH)) {
            lines.add(line);
            colors.add(color);
        }
        lines.add(FormattedCharSequence.EMPTY);
        colors.add(color);
    }

    // The host formats its numbers with grouping separators; String.format's own locale keeps the same text.
    private static String count(int value) {
        return String.format("%,d", value);
    }
}
