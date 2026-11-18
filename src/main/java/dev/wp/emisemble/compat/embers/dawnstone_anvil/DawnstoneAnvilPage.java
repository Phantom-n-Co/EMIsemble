package dev.wp.emisemble.compat.embers.dawnstone_anvil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.rekindled.embers.recipe.IDawnstoneAnvilRecipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.embers.EmbersCompat;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** One page for a whole anvil recipe; the three rows share a seed because entry <em>i</em> of each is one variant, so
 * tool, material and result stay together instead of the host's hundreds of pages. */
public final class DawnstoneAnvilPage extends BasicEmiRecipe {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(DawnstoneAnvilCategory.NAMESPACE, "textures/gui/jei_dawnstone_anvil.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 110;
    private static final int HEIGHT = 58;
    private static final int TOP_X = 22;
    private static final int TOP_Y = 19;
    private static final int BOTTOM_X = 22;
    private static final int BOTTOM_Y = 37;
    private static final int OUTPUT_X = 77;
    private static final int OUTPUT_Y = 28;
    private static final int VARIANT_SEED = 1;

    private final List<ItemStack> tops;
    private final List<ItemStack> bottoms;
    private final List<ItemStack> results;

    public DawnstoneAnvilPage(ResourceLocation id, List<IDawnstoneAnvilRecipe> variants) {
        super(DawnstoneAnvilCategory.CATEGORY, id, WIDTH, HEIGHT);
        this.tops = row(variants, IDawnstoneAnvilRecipe::getDisplayInputTop);
        this.bottoms = row(variants, IDawnstoneAnvilRecipe::getDisplayInputBottom);
        this.results = row(variants, IDawnstoneAnvilRecipe::getDisplayOutput);
        if (!tops.isEmpty()) {
            this.inputs.add(EmbersCompat.items(tops));
        }
        if (!bottoms.isEmpty()) {
            this.inputs.add(EmbersCompat.items(bottoms));
        }
        for (ItemStack stack : results) {
            this.outputs.add(EmiStack.of(stack));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
        slot(widgets, tops, TOP_X, TOP_Y).recipeContext(this);
        slot(widgets, bottoms, BOTTOM_X, BOTTOM_Y);
        slot(widgets, results, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }

    // A row with one entry is a plain slot: GeneratedSlotWidget draws its cycle badge, which claims a choice the page
    // does not have.
    private static SlotWidget slot(WidgetHolder widgets, List<ItemStack> stacks, int x, int y) {
        if (stacks.isEmpty()) {
            return RecipeSlots.addEmpty(widgets, x, y);
        }
        if (stacks.size() == 1) {
            return RecipeSlots.addFramed(widgets, EmiStack.of(stacks.getFirst()), x, y);
        }
        return RecipeSlots.addGenerated(widgets, random -> pick(stacks, random), VARIANT_SEED, x, y);
    }

    private static List<ItemStack> row(List<IDawnstoneAnvilRecipe> variants,
            java.util.function.Function<IDawnstoneAnvilRecipe, List<ItemStack>> accessor) {
        List<ItemStack> stacks = new ArrayList<>(variants.size());
        for (IDawnstoneAnvilRecipe variant : variants) {
            ItemStack stack = first(accessor.apply(variant));
            if (stack != null) {
                stacks.add(stack);
            }
        }
        // A row that repeats one stack on every variant is constant - the ingredient a repair is grouped by - and shows
        // as a plain slot instead of a cycle badge over twenty identical entries.
        for (ItemStack stack : stacks) {
            if (!ItemStack.isSameItemSameComponents(stack, stacks.getFirst())) {
                return stacks;
            }
        }
        return stacks.isEmpty() ? stacks : List.of(stacks.getFirst());
    }

    // A row's representative: the first non-empty stack. The whole choice stays in getInputs(), so a lookup on any of
    // the row's options still finds the page.
    private static ItemStack first(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return null;
    }

    private static EmiIngredient pick(List<ItemStack> stacks, Random random) {
        return EmiStack.of(stacks.get(random.nextInt(stacks.size())));
    }
}
