package dev.wp.emisemble.compat.mythsandlegends.research;

import com.ovinter.mythsandlegends.registry.MLItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.mythsandlegends.MythsAndLegendsCompat;
import dev.wp.emisemble.network.ResearchPayload;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** {@code mythsandlegends:research_definition}. */
public final class ResearchCategory {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mythsandlegends", "textures/gui/container/research.png");
    // 256 is the sheet JEI assumes for the host's 150px art; declaring the real size leaves the card bare.
    static final int SHEET = 256;
    // EMI puts the card 4px outside the widgets, so the page is the art less 8.
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
        List<ResearchPayload.Entry> entries = new ArrayList<>(ResearchSync.views());
        entries.sort(Comparator.comparingInt(ResearchPayload.Entry::requiredRank)
                .thenComparing(ResearchPayload.Entry::name));
        int added = 0;
        for (ResearchPayload.Entry entry : entries) {
            if (EmiRecipes.add(registry, new ResearchEmiRecipe(CATEGORY, entry))) {
                added++;
            }
        }
        if (entries.isEmpty()) {
            Emisemble.LOGGER.warn("mythsandlegends: no research data; a remote server needs this mod to send it");
        }
        Emisemble.LOGGER.info("mythsandlegends: {} research recipes", added);
        return added;
    }
}
