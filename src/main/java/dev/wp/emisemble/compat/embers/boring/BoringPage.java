package dev.wp.emisemble.compat.embers.boring;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.rekindled.embers.recipe.IBoringRecipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.embers.EmbersCompat;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/** A bore page, shared by boring and excavation: the drop above the block that yields it, then the host's filters. */
public final class BoringPage extends BasicEmiRecipe {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(BoringCategory.NAMESPACE, "textures/gui/jei_boring.png");
    static final int SHEET = 256;
    static final int WIDTH = 126;
    static final int HEIGHT = 98;

    private static final int OUTPUT_X = 6;
    private static final int OUTPUT_Y = 6;
    private static final int CATALYST_X = 6;
    private static final int CATALYST_Y = 26;
    private static final int TEXT_X = 28;
    private static final int FILTER_X = 7;
    private static final int FILTER_WIDTH = 112;
    private static final int LINE = 11;
    private static final int WHITE = 0xFFFFFF;
    private static final int HIGHLIGHT = 0xFFB54D;

    private final IBoringRecipe recipe;

    public BoringPage(EmiRecipeCategory category, RecipeHolder<IBoringRecipe> holder) {
        super(category, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();
        this.outputs.add(EmiStack.of(recipe.getDisplayOutput().getStack()));
        this.catalysts.add(EmbersCompat.items(recipe.getDisplayInput()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
        RecipeSlots.addFramed(widgets, outputs.get(0), OUTPUT_X, OUTPUT_Y).recipeContext(this);
        RecipeSlots.addFramed(widgets, catalysts.get(0), CATALYST_X, CATALYST_Y);

        widgets.addText(Component.translatable("embers.jei.recipe.boring.weight",
                recipe.getDisplayOutput().getWeight().asInt()), TEXT_X, 10, WHITE, true);
        widgets.addText(Component.translatable("embers.jei.recipe.boring.required_blocks"), TEXT_X, 30, WHITE, true);

        int y = 48;
        if (recipe.getMinHeight() != Integer.MIN_VALUE) {
            widgets.addText(Component.translatable("embers.jei.recipe.boring.min_height", recipe.getMinHeight()),
                    FILTER_X + 3, y, WHITE, true);
            y += LINE;
        }
        if (recipe.getMaxHeight() != Integer.MAX_VALUE) {
            widgets.addText(Component.translatable("embers.jei.recipe.boring.max_height", recipe.getMaxHeight()),
                    FILTER_X + 3, y, WHITE, true);
            y += LINE;
        }
        if (!recipe.getDimensions().isEmpty()) {
            widgets.addText(Component.translatable("embers.jei.recipe.boring.dimensions"), FILTER_X + 3, y,
                    HIGHLIGHT, true);
            widgets.addTooltip(EmiRecipes.tooltip(dimensions()), FILTER_X, y, FILTER_WIDTH, LINE);
            y += LINE;
        }
        if (!recipe.getBiomes().isEmpty()) {
            widgets.addText(Component.translatable("embers.jei.recipe.boring.biomes"), FILTER_X + 3, y, HIGHLIGHT, true);
            widgets.addTooltip(EmiRecipes.tooltip(biomes()), FILTER_X, y, FILTER_WIDTH, LINE);
        }
    }

    // The page is a description of a world interaction, so there is nothing to craft from it.
    @Override
    public boolean hideCraftable() {
        return true;
    }

    private List<Component> dimensions() {
        List<Component> lines = new ArrayList<>();
        for (ResourceLocation dimension : recipe.getDimensions()) {
            lines.add(Component.literal(WordUtils.capitalize(dimension.getPath().replace('_', ' '))));
        }
        return lines;
    }

    private List<Component> biomes() {
        List<Component> lines = new ArrayList<>();
        for (ResourceLocation biome : recipe.getBiomes()) {
            lines.add(Component.literal(WordUtils.capitalize(biome.getPath().replace('_', ' '))));
        }
        return lines;
    }
}
