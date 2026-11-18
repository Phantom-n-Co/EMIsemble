package dev.wp.emisemble.compat.mythsandlegends.research;

import com.ovinter.mythsandlegends.api.research.ResearchDefinition;
import com.ovinter.mythsandlegends.api.research.ResearchManager;
import com.ovinter.mythsandlegends.registry.MLItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.mythsandlegends.MythsAndLegendsCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

/** {@code mythsandlegends:research_definition}. */
public final class ResearchCategory {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mythsandlegends", "textures/gui/container/research.png");
    // JEI's createDrawable assumes a 256x256 sheet whatever the PNG is, so the host's 150x133 page samples only the
    // art's opaque 88x78 corner and blows it up to the whole card; the real sheet size would leave the card bare.
    static final int SHEET = 256;
    // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
    static final int WIDTH = 142;
    static final int HEIGHT = 125;

    private static final EmiStack ICON = EmiStack.of(new ItemStack(MLItems.RESEARCH_NOTES.get()));

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            MythsAndLegendsCompat.categoryId("research_definition"), ICON, "jei.mythsandlegends.research");

    private ResearchCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("mythsandlegends: could not register the research category", t);
            return 0;
        }
        // The host's own page order: the rank a research unlocks at, then its name.
        List<ResearchDefinition> definitions = ResearchManager.getAll().stream()
                .sorted(Comparator.comparingInt(ResearchDefinition::requiredRank)
                        .thenComparing(ResearchDefinition::name))
                .toList();
        int added = 0;
        for (ResearchDefinition definition : definitions) {
            if (EmiRecipes.add(registry, new ResearchEmiRecipe(CATEGORY, definition))) {
                added++;
            }
        }
        Emisemble.LOGGER.info("mythsandlegends: {} research recipes", added);
        return added;
    }
}
