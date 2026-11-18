package dev.wp.emisemble.compat.thaumaturge.aspectpages;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.thaumaturge.AspectEmiStack;
import dev.wp.emisemble.compat.thaumaturge.ThaumaturgeCompat;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** One aspect above the items that carry it, nine to a row, each item counted by how much of the aspect it holds. */
final class AspectFromStacksEmiRecipe extends BasicEmiRecipe {
    static final int WIDTH = 176;
    static final int HEIGHT = 109;

    // JEI coordinates; RecipeSlots applies the -1 every slot needs.
    private static final int RESULT_X = 80;
    private static final int RESULT_Y = 8;
    private static final int GRID_X = 6;
    private static final int GRID_Y = 32;
    private static final int STEP = 18;
    private static final int COLUMNS = 9;

    private static final int FRAME_X = 72;
    private static final int FRAME_Y = 0;
    private static final int FRAME_SIZE = 32;
    private static final int FRAME_U = 40;
    private static final int FRAME_V = 6;

    // The page sheet is blitted at half opacity over the frame.
    private static final ResourceLocation INNER =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "textures/gui/gui_inner.png");
    private static final int INNER_SHEET = 256;
    private static final int INNER_X = 5;
    private static final int INNER_Y = 30;
    private static final int INNER_WIDTH = 163;
    private static final int INNER_HEIGHT = 74;
    private static final float INNER_ALPHA = 0.5F;

    private final Holder<IAspect> aspect;
    private final List<ItemStack> stacks;

    AspectFromStacksEmiRecipe(EmiRecipeCategory category, ResourceLocation id, Holder<IAspect> aspect,
                              List<ItemStack> stacks) {
        super(category, id, WIDTH, HEIGHT);
        this.aspect = aspect;
        this.stacks = List.copyOf(stacks);
        for (ItemStack stack : this.stacks) {
            this.inputs.add(EmiStack.of(stack));
        }
        this.outputs.add(new AspectEmiStack(aspect, 1));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Both pieces first: JEI draws the category before the slots, so the chips stay on top of the page.
        ThaumaturgeCompat.art(widgets, FRAME_X, FRAME_Y, FRAME_SIZE, FRAME_SIZE, FRAME_U, FRAME_V);
        AlphaTextures.add(widgets, INNER, INNER_SHEET, INNER_X, INNER_Y, INNER_WIDTH, INNER_HEIGHT, 0, 0, INNER_ALPHA);

        RecipeSlots.add(widgets, new AspectEmiStack(aspect, 1), RESULT_X, RESULT_Y).recipeContext(this);

        for (int i = 0; i < stacks.size(); i++) {
            int x = GRID_X + (i % COLUMNS) * STEP;
            int y = GRID_Y + (i / COLUMNS) * STEP;
            RecipeSlots.add(widgets, EmiStack.of(stacks.get(i)), x, y);
        }
    }

    /** The page is an index, not a way to obtain the aspect: keep it out of the craftables sidebar. */
    @Override
    public boolean hideCraftable() {
        return true;
    }

    @Nullable
    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return null;
    }
}
