package dev.wp.emisemble.compat.apotheosis.stoneforming;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.effect.StoneformingAffix;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** {@code apotheosis:stoneforming}: one page per stoneforming affix, since the host synthesises the category. */
public final class StoneformingCategory {
    // The host's uid is a JEI-only RecipeType.create, so there is no registry key to read it from.
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            ResourceLocation.fromNamespaceAndPath("apotheosis", "stoneforming"),
            EmiStack.of(Blocks.STONE),
            "title.apotheosis.stoneforming");

    private StoneformingCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);

        // The host's own filter and order, so both viewers list the pages identically.
        List<Affix> affixes = AffixRegistry.INSTANCE.getValues().stream()
                .filter(StoneformingAffix.class::isInstance)
                .sorted(Comparator.comparing(Affix::id))
                .toList();

        int added = 0;
        for (Affix affix : affixes) {
            StoneformingAffix stoneforming = (StoneformingAffix) affix;
            try {
                List<EmiStack> candidates = new ArrayList<>();
                for (Holder<Block> candidate : stoneforming.getCandidates()) {
                    ItemStack stack = new ItemStack(candidate.value());
                    // A block with no item form is not a slot in either viewer, matching the host.
                    if (!stack.isEmpty()) {
                        candidates.add(EmiStack.of(stack));
                    }
                }
                added += addPages(registry, affix.id(), affix.getName(true), EmiIngredient.of(candidates), candidates);
            } catch (Throwable t) {
                // One bad affix must cost one page: a throw escaping register costs the plugin every category.
                Emisemble.LOGGER.warn("Skipping apotheosis stoneforming page for {}", affix.id(), t);
            }
        }

        Emisemble.LOGGER.info("apotheosis: {} stoneforming recipes registered", added);
        return added;
    }

    /** One page per {@link StoneformingEmiRecipe#PAGE_SIZE} candidates: JEI scrolls its grid, an EMI page cannot. */
    private static int addPages(EmiRegistry registry, ResourceLocation affixId, Component title, EmiIngredient input,
                                List<EmiStack> candidates) {
        int size = StoneformingEmiRecipe.PAGE_SIZE;
        int pages = Math.max(1, (candidates.size() + size - 1) / size);

        int added = 0;
        for (int page = 0; page < pages; page++) {
            int from = Math.min(page * size, candidates.size());
            int to = Math.min(from + size, candidates.size());
            StoneformingEmiRecipe recipe = new StoneformingEmiRecipe(
                    CATEGORY, idOf(affixId, page), title, input, candidates.subList(from, to));
            if (EmiRecipes.add(registry, recipe)) {
                added++;
            }
        }
        return added;
    }

    /** A synthetic id: the affix's own, behind the category segment, so the page traces back to its affix. */
    private static ResourceLocation idOf(ResourceLocation affixId, int page) {
        String suffix = page == 0 ? "" : "/" + (page + 1);
        return ResourceLocation.fromNamespaceAndPath(affixId.getNamespace(),
                "/stoneforming/" + affixId.getPath() + suffix);
    }
}
